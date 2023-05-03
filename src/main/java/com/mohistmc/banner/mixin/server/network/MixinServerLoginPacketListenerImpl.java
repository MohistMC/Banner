package com.mohistmc.banner.mixin.server.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.bukkit.craftbukkit.v1_19_R3.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class MixinServerLoginPacketListenerImpl {

    @Shadow public abstract void disconnect(Component reason);

    @Shadow @Nullable private GameProfile gameProfile;

    @Shadow protected abstract GameProfile createFakeProfile(GameProfile original);

    @Shadow @Final private MinecraftServer server;

    @Shadow @Final public Connection connection;

    @Shadow private ServerLoginPacketListenerImpl.State state;

    @Shadow @Nullable private ServerPlayer delayedAcceptPlayer;

    @Shadow @Final private static Logger LOGGER;

    @Shadow protected abstract void placeNewPlayer(ServerPlayer serverPlayer);

    // CraftBukkit start
    @Deprecated
    public void disconnect(String s) {
        disconnect(Component.literal(s));
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void handleAcceptedLogin() {
        if (!this.gameProfile.isComplete()) {
            this.gameProfile = this.createFakeProfile(this.gameProfile);
        }

        Component component = this.server.getPlayerList().canPlayerLogin(this.connection.getRemoteAddress(), this.gameProfile);
        if (component != null) {
            this.disconnect(component);
        } else {
            this.state = ServerLoginPacketListenerImpl.State.ACCEPTED;
            if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
                this.connection.send(new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()), PacketSendListener.thenRun(() -> {
                    this.connection.setupCompression(this.server.getCompressionThreshold(), true);
                }));
            }

            this.connection.send(new ClientboundGameProfilePacket(this.gameProfile));
            ServerPlayer serverPlayer = this.server.getPlayerList().getPlayer(this.gameProfile.getId());

            try {
                ServerPlayer serverPlayer2 = this.server.getPlayerList().getPlayerForLogin(this.gameProfile);
                if (serverPlayer != null) {
                    // CraftBukkit start - fire PlayerPreLoginEvent
                    if (!connection.isConnected()) {
                        return;
                    }

                    String playerName = gameProfile.getName();
                    java.net.InetAddress address = ((java.net.InetSocketAddress) connection.getRemoteAddress()).getAddress();
                    java.util.UUID uniqueId = gameProfile.getId();
                    final org.bukkit.craftbukkit.v1_19_R3.CraftServer server = this.server.bridge$server();

                    AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(playerName, address, uniqueId);
                    server.getPluginManager().callEvent(asyncEvent);
                    if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
                        final PlayerPreLoginEvent event = new PlayerPreLoginEvent(playerName, address, uniqueId);
                        if (asyncEvent.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
                            event.disallow(asyncEvent.getResult(), asyncEvent.getKickMessage());
                        }
                        Waitable<PlayerPreLoginEvent.Result> waitable = new Waitable<PlayerPreLoginEvent.Result>() {
                            @Override
                            protected PlayerPreLoginEvent.Result evaluate() {
                                server.getPluginManager().callEvent(event);
                                return event.getResult();
                            }
                        };

                        this.server.bridge$processQueue().add(waitable);
                        if (waitable.get() != PlayerPreLoginEvent.Result.ALLOWED) {
                            disconnect(event.getKickMessage());
                            return;
                        }
                    } else {
                        if (asyncEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                            disconnect(asyncEvent.getKickMessage());
                            return;
                        }
                    }
                    // CraftBukkit end
                    this.state = ServerLoginPacketListenerImpl.State.DELAY_ACCEPT;
                    this.delayedAcceptPlayer = serverPlayer2;
                } else {
                    this.placeNewPlayer(serverPlayer2);
                }
            } catch (Exception var5) {
                LOGGER.error("Couldn't place player in world", var5);
                Component component2 = Component.translatable("multiplayer.disconnect.invalid_player_data");
                this.connection.send(new ClientboundDisconnectPacket(component2));
                this.connection.disconnect(component2);
                // CraftBukkit start - catch all exceptions
                disconnect("Failed to verify username!");
                server.bridge$server().getLogger().log(java.util.logging.Level.WARNING, "Exception verifying " + gameProfile.getName(), var5);
                // CraftBukkit end
            }
        }

    }
}
