package com.mohistmc.banner.mixin.core.server.network;

import java.net.InetAddress;
import java.util.HashMap;
import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.StatusProtocols;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//TODO fixed
@Mixin(ServerHandshakePacketListenerImpl.class)
public abstract class MixinServerHandshakePacketListenerImpl implements ServerHandshakePacketListener {

    @Shadow @Final private Connection connection;
    @Shadow @Final private MinecraftServer server;
    // CraftBukkit start - add fields
    private static final HashMap<InetAddress, Long> throttleTracker = new HashMap<InetAddress, Long>();
    private static int throttleCounter = 0;
    // CraftBukkit end

    @Inject(method = "handleIntention", at = @At("HEAD"))
    private void banner$setHostName(ClientIntentionPacket clientIntentionPacket, CallbackInfo ci) {
        this.connection.banner$setHostName(clientIntentionPacket.hostName() + ":" + clientIntentionPacket.port()); // CraftBukkit  - set hostname
    }

    @Inject(method = "beginLogin", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/handshake/ClientIntentionPacket;protocolVersion()I",
            ordinal = 0))
    private void banner$handleThrottle(ClientIntentionPacket clientIntentionPacket, boolean bl, CallbackInfo ci) {
        // CraftBukkit start - Connection throttle
        try {
            long currentTime = System.currentTimeMillis();
            long connectionThrottle = this.server.bridge$server().getConnectionThrottle();
            InetAddress address = ((java.net.InetSocketAddress) this.connection.getRemoteAddress()).getAddress();

            synchronized (throttleTracker) {
                if (throttleTracker.containsKey(address) && !"127.0.0.1".equals(address.getHostAddress()) && currentTime - throttleTracker.get(address) < connectionThrottle) {
                    throttleTracker.put(address, currentTime);
                    MutableComponent chatmessage = Component.literal("Connection throttled! Please wait before reconnecting.");
                    this.connection.send(new ClientboundLoginDisconnectPacket(chatmessage));
                    this.connection.disconnect(chatmessage);
                    return;
                }

                throttleTracker.put(address, currentTime);
                throttleCounter++;
                if (throttleCounter > 200) {
                    throttleCounter = 0;

                    // Cleanup stale entries
                    java.util.Iterator iter = throttleTracker.entrySet().iterator();
                    while (iter.hasNext()) {
                        java.util.Map.Entry<InetAddress, Long> entry = (java.util.Map.Entry) iter.next();
                        if (entry.getValue() > connectionThrottle) {
                            iter.remove();
                        }
                    }
                }
            }
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger().debug("Failed to check connection throttle", t);
        }
        // CraftBukkit end
    }
}
