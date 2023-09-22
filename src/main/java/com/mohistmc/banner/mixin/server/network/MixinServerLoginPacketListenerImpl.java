package com.mohistmc.banner.mixin.server.network;

import com.mohistmc.banner.config.BannerConfig;
import com.mohistmc.banner.config.BannerConfigUtil;
import com.mohistmc.banner.injection.server.network.InjectionServerLoginPacketListenerImpl;
import com.mojang.authlib.GameProfile;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.login.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class MixinServerLoginPacketListenerImpl implements ServerLoginPacketListener, TickablePacketListener,InjectionServerLoginPacketListenerImpl {

    @Shadow public abstract void disconnect(Component reason);

    @Shadow @Nullable GameProfile gameProfile;

    @Shadow protected abstract GameProfile createFakeProfile(GameProfile original);

    @Shadow @Final
    MinecraftServer server;

    @Shadow @Final public Connection connection;

    @Shadow
    ServerLoginPacketListenerImpl.State state;

    @Shadow @Nullable private ServerPlayer delayedAcceptPlayer;

    @Shadow @Final private static Logger LOGGER;

    @Shadow protected abstract void placeNewPlayer(ServerPlayer serverPlayer);

    @Shadow @Final private byte[] challenge;

    @Shadow @Final private static AtomicInteger UNIQUE_THREAD_ID;

    // CraftBukkit start
    @Deprecated
    @Override
    public void disconnect(String s) {
        disconnect(Component.literal(s));
    }

    // Spigot start
    public void initUUID()
    {
        UUID uid =  UUIDUtil.createOfflinePlayerUUID( this.gameProfile.getName() );
        this.gameProfile = new GameProfile( uid, this.gameProfile.getName() );
    }
    // Spigot end

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void handleAcceptedLogin() {
        if (!this.gameProfile.isComplete()) {
            // this.gameProfile = this.createFakeProfile(this.gameProfile);
        }

        // CraftBukkit start - fire PlayerLoginEvent
        ServerPlayer s = this.server.getPlayerList().canPlayerLogin(((ServerLoginPacketListenerImpl) (Object) this), this.gameProfile);

        if (s == null) {
            // this.disconnect(ichatbasecomponent);
            // CraftBukkit end
        } else {
            this.state = ServerLoginPacketListenerImpl.State.ACCEPTED;
            if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
                this.connection.send(new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()), PacketSendListener.thenRun(() -> {
                    this.connection.setupCompression(this.server.getCompressionThreshold(), true);
                }));
            }

            this.connection.send(new ClientboundGameProfilePacket(this.gameProfile));
            ServerPlayer entityplayer = this.server.getPlayerList().getPlayer(this.gameProfile.getId());

            try {
                ServerPlayer entityplayer1 = this.server.getPlayerList().getPlayerForLogin(this.gameProfile, s); // CraftBukkit - add player reference

                if (entityplayer != null) {
                    this.state = ServerLoginPacketListenerImpl.State.DELAY_ACCEPT;
                    this.delayedAcceptPlayer = entityplayer1;
                } else {
                    this.placeNewPlayer(entityplayer1);
                }
            } catch (Exception exception) {
                LOGGER.error("Couldn't place player in world", exception);
                MutableComponent ichatmutablecomponent = Component.translatable("multiplayer.disconnect.invalid_player_data");

                this.connection.send(new ClientboundDisconnectPacket(ichatmutablecomponent));
                this.connection.disconnect(ichatmutablecomponent);
            }
        }
    }

    // Paper start - Cache authenticator threads
    private static final AtomicInteger threadId = new AtomicInteger(0);
    private static final java.util.concurrent.ExecutorService authenticatorPool = java.util.concurrent.Executors.newCachedThreadPool(
            r -> {
                Thread ret = new Thread(r, "User Authenticator #" + threadId.incrementAndGet());

                ret.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));

                return ret;
            }
    );

    @Unique
    private int velocityLoginMessageId = -1;    // Paper - Velocity support.
    // Paper end

    /**
     * @author wdog5
     * @reason bukkit
     */
    public void handleHello(ServerboundHelloPacket packet) {
        Validate.validState(this.state == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet", new Object[0]);
        // Validate.validState(isValidUsername(packet.name()), "Invalid characters in username", new Object[0]); // Mohist Chinese and other special characters are allowed
        GameProfile gameProfile = this.server.getSingleplayerProfile();
        if (gameProfile != null && packet.name().equalsIgnoreCase(gameProfile.getName())) {
            this.gameProfile = gameProfile;
            this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
        } else {
            this.gameProfile = new GameProfile((UUID)null, packet.name());
            if (this.server.usesAuthentication() && !this.connection.isMemoryConnection()) {
                this.state = ServerLoginPacketListenerImpl.State.KEY;
                this.connection.send(new ClientboundHelloPacket("", this.server.getKeyPair().getPublic().getEncoded(), this.challenge));
            } else {
                // Paper start - Velocity support
                if (BannerConfig.velocityEnabled) {
                    this.velocityLoginMessageId = java.util.concurrent.ThreadLocalRandom.current().nextInt();
                    net.minecraft.network.FriendlyByteBuf buf = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                    buf.writeByte(com.destroystokyo.paper.proxy.VelocityProxy.MAX_SUPPORTED_FORWARDING_VERSION);
                    net.minecraft.network.protocol.login.ClientboundCustomQueryPacket packet1 = new net.minecraft.network.protocol.login.ClientboundCustomQueryPacket(this.velocityLoginMessageId, com.destroystokyo.paper.proxy.VelocityProxy.PLAYER_INFO_CHANNEL, buf);
                    this.connection.send(packet1);
                    return;
                }
                // Paper end
                // Spigot start
                // Paper start - Cache authenticator threads
                authenticatorPool.execute(() -> {
                    try {
                        this.initUUID();
                        banner$preLogin();
                    } catch (Exception ex) {
                        this.disconnect("Failed to verify username!");
                        LOGGER.warn("Exception verifying " + this.gameProfile.getName(), ex);
                    }
                });
                // Paper end
                // Spigot end
            }

        }
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void handleKey(ServerboundKeyPacket packetIn) {
        Validate.validState(this.state == ServerLoginPacketListenerImpl.State.KEY, "Unexpected key packet");

        final String s;
        try {
            PrivateKey privatekey = this.server.getKeyPair().getPrivate();
            if (!packetIn.isChallengeValid(this.challenge, privatekey)) {
                throw new IllegalStateException("Protocol error");
            }

            SecretKey secretKey = packetIn.getSecretKey(privatekey);
            Cipher cipher = Crypt.getCipher(2, secretKey);
            Cipher cipher1 = Crypt.getCipher(1, secretKey);
            s = (new BigInteger(Crypt.digestData("", this.server.getKeyPair().getPublic(), secretKey))).toString(16);
            this.state = ServerLoginPacketListenerImpl.State.AUTHENTICATING;
            this.connection.setEncryptionKey(cipher, cipher1);
        } catch (CryptException cryptexception) {
            throw new IllegalStateException("Protocol error", cryptexception);
        }

        class Handler extends Thread {

            Handler(int i) {
                super("User Authenticator #" + i);
            }

            public void run() {
                GameProfile gameprofile = gameProfile;

                try {
                    gameProfile = server.getSessionService().hasJoinedServer(new GameProfile(null, gameprofile.getName()), s, this.getAddress());
                    if (gameProfile != null) {
                        if (!connection.isConnected()) {
                            return;
                        }
                        banner$preLogin();
                    } else if (server.isSingleplayer()) {
                        LOGGER.warn("Failed to verify username but will let them in anyway!");
                        gameProfile = createFakeProfile(gameprofile);
                        state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
                    } else {
                        disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
                        LOGGER.error("Username '{}' tried to join with an invalid session", gameprofile.getName());
                    }
                } catch (Exception var3) {
                    if (server.isSingleplayer()) {
                        LOGGER.warn("Authentication servers are down but will let them in anyway!");
                        gameProfile = createFakeProfile(gameprofile);
                        state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
                    } else {
                        disconnect(Component.translatable("multiplayer.disconnect.authservers_down"));
                        LOGGER.error("Couldn't verify username because servers are unavailable");
                    }
                }

            }

            @Nullable
            private InetAddress getAddress() {
                SocketAddress socketaddress = connection.getRemoteAddress();
                return server.getPreventProxyConnections() && socketaddress instanceof InetSocketAddress ? ((InetSocketAddress) socketaddress).getAddress() : null;
            }
        }
        Thread thread = new Handler(UNIQUE_THREAD_ID.incrementAndGet());
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    void banner$preLogin() throws Exception {
        if (velocityLoginMessageId == -1 && BannerConfig.velocityEnabled) {
            disconnect("This server requires you to connect with Velocity.");
            return;
        }

        String playerName = gameProfile.getName();
        InetAddress address = ((InetSocketAddress) connection.getRemoteAddress()).getAddress();
        UUID uniqueId = gameProfile.getId();
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(playerName, address, uniqueId);
        craftServer.getPluginManager().callEvent(asyncEvent);
        if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
            PlayerPreLoginEvent event = new PlayerPreLoginEvent(playerName, address, uniqueId);
            if (asyncEvent.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
                event.disallow(asyncEvent.getResult(), asyncEvent.getKickMessage());
            }
            class SyncPreLogin extends Waitable<PlayerPreLoginEvent.Result> {

                @Override
                protected PlayerPreLoginEvent.Result evaluate() {
                    craftServer.getPluginManager().callEvent(event);
                    return event.getResult();
                }
            }
            Waitable<PlayerPreLoginEvent.Result> waitable = new SyncPreLogin();
            server.bridge$queuedProcess(waitable);
            if (waitable.get() != PlayerPreLoginEvent.Result.ALLOWED) {
                disconnect(event.getKickMessage());
                return;
            }
        } else if (asyncEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            disconnect(asyncEvent.getKickMessage());
            return;
        }
        LOGGER.info("UUID of player {} is {}", gameProfile.getName(), gameProfile.getId());
        state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
    }

    @Inject(method = "handleCustomQueryPacket", at = @At("HEAD"), cancellable = true)
    private void beforeCustomQuery(ServerboundCustomQueryPacket packet, CallbackInfo ci) {
        // Paper start - Velocity support
        if (BannerConfig.velocityEnabled && packet.getTransactionId() == this.velocityLoginMessageId) {
            net.minecraft.network.FriendlyByteBuf buf = packet.getData();
            if (buf == null) {
                this.disconnect("This server requires you to connect with Velocity.");
                ci.cancel();
                return;
            }

            if (!com.destroystokyo.paper.proxy.VelocityProxy.checkIntegrity(buf)) {
                this.disconnect("Unable to verify player details");
                ci.cancel();
                return;
            }

            int version = buf.readVarInt();
            if (version > com.destroystokyo.paper.proxy.VelocityProxy.MAX_SUPPORTED_FORWARDING_VERSION) {
                throw new IllegalStateException("Unsupported forwarding version " + version + ", wanted upto " + com.destroystokyo.paper.proxy.VelocityProxy.MAX_SUPPORTED_FORWARDING_VERSION);
            }

            java.net.SocketAddress listening = this.connection.getRemoteAddress();
            int port = 0;
            if (listening instanceof java.net.InetSocketAddress) {
                port = ((java.net.InetSocketAddress) listening).getPort();
            }
            this.connection.address = new java.net.InetSocketAddress(com.destroystokyo.paper.proxy.VelocityProxy.readAddress(buf), port);

            this.gameProfile = com.destroystokyo.paper.proxy.VelocityProxy.createProfile(buf);

            //TODO Update handling for lazy sessions, might not even have to do anything?

            // Proceed with login
            authenticatorPool.execute(() -> {
                try {
                    banner$preLogin();
                } catch (Exception ex) {
                    this.disconnect("Failed to verify username!");
                    LOGGER.warn("Exception verifying " + gameProfile.getName(), ex);
                }
            });
            ci.cancel();
        }
        // Paper end
    }
}
