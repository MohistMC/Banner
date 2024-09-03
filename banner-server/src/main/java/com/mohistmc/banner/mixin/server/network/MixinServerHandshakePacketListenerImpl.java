package com.mohistmc.banner.mixin.server.network;

import com.destroystokyo.paper.proxy.VelocitySupport;
import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UndashedUuid;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.spigotmc.SpigotConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
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
    private static final Gson gson = new Gson();
    private static final java.util.regex.Pattern HOST_PATTERN = java.util.regex.Pattern.compile("[0-9a-f\\.:]{0,45}");


    @Inject(method = "handleIntention", at = @At("HEAD"))
    private void banner$setHostName(ClientIntentionPacket packet, CallbackInfo ci) {
        this.connection.banner$setHostName(packet.hostName() + ":" + packet.port()); // CraftBukkit  - set hostname
    }

    @Inject(method = "beginLogin", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/Connection;setupOutboundProtocol(Lnet/minecraft/network/ProtocolInfo;)V"))
    private void banner$throttler(ClientIntentionPacket packet, boolean bl, CallbackInfo ci) {
        try {
            long currentTime = System.currentTimeMillis();
            long connectionThrottle = Bukkit.getServer().getConnectionThrottle();
            InetAddress address = ((InetSocketAddress) this.connection.getRemoteAddress()).getAddress();
            synchronized (throttleTracker) {
                if (throttleTracker.containsKey(address) && !"127.0.0.1".equals(address.getHostAddress()) && currentTime - throttleTracker.get(address) < connectionThrottle) {
                    throttleTracker.put(address, currentTime);
                    var component = Component.literal("Connection throttled! Please wait before reconnecting.");
                    this.connection.send(new ClientboundLoginDisconnectPacket(component));
                    this.connection.disconnect(component);
                    ci.cancel();
                    return;
                }
                throttleTracker.put(address, currentTime);
                ++throttleCounter;
                if (throttleCounter > 200) {
                    throttleCounter = 0;
                    throttleTracker.entrySet().removeIf(entry -> entry.getValue() > connectionThrottle);
                }
            }
        } catch (Throwable t) {
            LogManager.getLogger().debug("Failed to check connection throttle", t);
        }
    }

    @Inject(method = "beginLogin", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V"))
    private void banner$proxySupport(ClientIntentionPacket packet, boolean bl, CallbackInfo ci) {
        if (!VelocitySupport.isEnabled()) {
            String[] split = packet.hostName().split("\00");
            if (SpigotConfig.bungee) {
                if ((split.length == 3 || split.length == 4) && (HOST_PATTERN.matcher(split[1]).matches())) {
                     this.connection.banner$setHostName(split[0]);
                    this.connection.address = new InetSocketAddress(split[1], ((InetSocketAddress) this.connection.getRemoteAddress()).getPort());
                     this.connection.banner$setSpoofedUUID(UndashedUuid.fromStringLenient(split[2]));
                } else {
                    var component = Component.literal("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!");
                    this.connection.send(new ClientboundLoginDisconnectPacket(component));
                    this.connection.disconnect(component);
                    ci.cancel();
                    return;
                }
                if (split.length == 4) {
                    this.connection.bridge$setSpoofedProfile(gson.fromJson(split[3], Property[].class));
                }
            } else if ((split.length == 3 || split.length == 4) && (HOST_PATTERN.matcher(split[1]).matches())) {
                Component component = Component.literal("Unknown data in login hostname, did you forget to enable BungeeCord in spigot.yml?");
                this.connection.send(new ClientboundLoginDisconnectPacket(component));
                this.connection.disconnect(component);
                ci.cancel();
            }
        }
    }
}
