package com.mohistmc.banner.mixin.server.network;

import com.mohistmc.banner.bukkit.BukkitMethodHooks;
import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import com.mohistmc.banner.injection.server.network.InjectionServerCommonPacketListenerImpl;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.netty.util.internal.StringUtil;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class MixinServerCommonPacketListenerImpl implements ServerCommonPacketListener, InjectionServerCommonPacketListenerImpl {

    @Shadow
    @Final
    protected Connection connection;
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    protected MinecraftServer server;

    @Shadow
    public abstract void send(Packet<?> p_300558_);

    @Shadow
    protected abstract boolean isSingleplayerOwner();
    // @formatter:on

    @Shadow public abstract void onDisconnect(DisconnectionDetails disconnectionDetails);

    @Shadow public abstract void disconnect(Component component);

    @Shadow @Final private boolean transferred;
    protected ServerPlayer player;
    protected CraftServer cserver;
    public boolean processedDisconnect;

    @Override
    public CraftPlayer getCraftPlayer() {
        return (this.player == null) ? null : this.player.getBukkitEntity();
    }

    @Override
    public void banner$setPlayer(ServerPlayer player) {
        this.player = player;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(MinecraftServer server, Connection connection, CommonListenerCookie cookie, CallbackInfo ci) {
        this.cserver = ((CraftServer) Bukkit.getServer());
    }

    @ModifyConstant(method = "keepConnectionAlive", constant = @Constant(longValue = 15000L))
    private long banner$incrKeepaliveTimeout(long l) {
        return 25000L;
    }

    @Override
    public boolean bridge$processedDisconnect() {
        return this.processedDisconnect;
    }

    public final boolean isDisconnected() {
        return !((ServerPlayer) this.player).bridge$joining() && !this.connection.isConnected();
    }

    @Override
    public boolean banner$isDisconnected() {
        return this.isDisconnected();
    }

    @Decorate(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V"))
    private void banner$kickEvent(Connection instance, Packet<?> packet, PacketSendListener packetSendListener, DisconnectionDetails disconnectionDetails) throws Throwable {
        if (this.processedDisconnect) {
            DecorationOps.cancel().invoke();
            return;
        }
        if (!this.cserver.isPrimaryThread()) {
            Waitable<?> waitable = new Waitable<>() {
                @Override
                protected Object evaluate() {
                    onDisconnect(disconnectionDetails);
                    return null;
                }
            };

            this.server.bridge$queuedProcess(waitable);

            try {
                waitable.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            DecorationOps.cancel().invoke();
            return;
        }
        String leaveMessage = ChatFormatting.YELLOW + this.player.getScoreboardName() + " left the game.";
        PlayerKickEvent event = new PlayerKickEvent(getCraftPlayer(), CraftChatMessage.fromComponent(disconnectionDetails.reason()), leaveMessage);
        if (this.cserver.getServer().isRunning()) {
            this.cserver.getPluginManager().callEvent(event);
        }
        if (event.isCancelled()) {
            DecorationOps.cancel().invoke();
            return;
        }
        BukkitSnapshotCaptures.captureQuitMessage(event.getLeaveMessage());
        Component textComponent = CraftChatMessage.fromString(event.getReason(), true)[0];
        Packet<?> newPacket = new ClientboundDisconnectPacket(textComponent);
        DecorationOps.callsite().invoke(instance, newPacket, packetSendListener);
        this.onDisconnect(disconnectionDetails);
    }

    @Override
    public void disconnect(String s) {
        this.disconnect(Component.literal(s));
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", cancellable = true, at = @At("HEAD"))
    private void banner$updateCompassTarget(Packet<?> packetIn, PacketSendListener futureListeners, CallbackInfo ci) {
        if (packetIn == null || processedDisconnect) {
            ci.cancel();
            return;
        }
        if (packetIn instanceof ClientboundSetDefaultSpawnPositionPacket packet6) {
            ((ServerPlayer) this.player).banner$setCompassTarget(new Location(this.getCraftPlayer().getWorld(), packet6.pos.getX(), packet6.pos.getY(), packet6.pos.getZ()));
        }
    }


    private static final ResourceLocation CUSTOM_REGISTER = ResourceLocation.parse("register");
    private static final ResourceLocation CUSTOM_UNREGISTER = ResourceLocation.parse("unregister");

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void banner$customPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        var data = bridge$getDiscardedData(packet);
        if (data != null) {
            var readerIndex = data.readerIndex();
            var buf = new byte[data.readableBytes()];
            data.readBytes(buf);
            data.readerIndex(readerIndex);
            BukkitMethodHooks.getServer().executeIfPossible(() -> {
                if (BukkitMethodHooks.getServer().hasStopped() || bridge$processedDisconnect()) {
                    return;
                }
                if (this.connection.isConnected()) {
                    if (packet.payload().type().id().equals(CUSTOM_REGISTER)) {
                        try {
                            String channels = new String(buf, StandardCharsets.UTF_8);
                            for (String channel : channels.split("\0")) {
                                if (!StringUtil.isNullOrEmpty(channel)) {
                                    this.getCraftPlayer().addChannel(channel);
                                }
                            }
                        } catch (Exception ex) {
                            LOGGER.error("Couldn't register custom payload", ex);
                            this.disconnect("Invalid payload REGISTER!");
                        }
                    } else if (packet.payload().type().id().equals(CUSTOM_UNREGISTER)) {
                        try {
                            final String channels = new String(buf, StandardCharsets.UTF_8);
                            for (String channel : channels.split("\0")) {
                                if (!StringUtil.isNullOrEmpty(channel)) {
                                    this.getCraftPlayer().removeChannel(channel);
                                }
                            }
                        } catch (Exception ex) {
                            LOGGER.error("Couldn't unregister custom payload", ex);
                            this.disconnect("Invalid payload UNREGISTER!");
                        }
                    } else {
                        try {
                            this.cserver.getMessenger().dispatchIncomingMessage(this.bridge$player().getBukkitEntity(), packet.payload().type().id().toString(), buf);
                        } catch (Exception ex) {
                            LOGGER.error("Couldn't dispatch custom payload", ex);
                            this.disconnect("Invalid custom payload!");
                        }
                    }
                }
            });
        }
    }

    public FriendlyByteBuf bridge$getDiscardedData(ServerboundCustomPayloadPacket packet) {
        var customPacketPayload = packet.payload();
        if (customPacketPayload instanceof DiscardedPayload b && b.bridge$getData() != null) {
            return new FriendlyByteBuf(b.bridge$getData());
        }
        return null;
    }

    @Inject(method = "handleResourcePackResponse", at = @At("RETURN"))
    private void banner$handleResourcePackStatus(ServerboundResourcePackPacket packetIn, CallbackInfo ci) {
        this.cserver.getPluginManager().callEvent(new PlayerResourcePackStatusEvent(this.getCraftPlayer(), packetIn.id(), PlayerResourcePackStatusEvent.Status.values()[packetIn.action().ordinal()]));
    }

    @Inject(method = "handleCookieResponse", cancellable = true, at = @At("HEAD"))
    private void banner$handleCookie(ServerboundCookieResponsePacket serverboundCookieResponsePacket, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(serverboundCookieResponsePacket, (ServerCommonPacketListenerImpl) (Object) this, this.server);
        if (((CraftPlayer) this.player.getBukkitEntity()).handleCookieResponse(serverboundCookieResponsePacket)) {
            ci.cancel();
        }
    }

    @Override
    public boolean isTransferred() {
        return this.transferred;
    }

    @Override
    public ConnectionProtocol getProtocol() {
        return this.protocol();
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        this.send(packet);
    }

    @Override
    public ServerPlayer bridge$player() {
        return player;
    }
}
