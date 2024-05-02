package com.mohistmc.banner.mixin.core.server.network;

import java.net.InetAddress;
import java.util.HashMap;
import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

//TODO fixed
@Mixin(ServerHandshakePacketListenerImpl.class)
public class MixinServerHandshakePacketListenerImpl {

    private static final HashMap<InetAddress, Long> throttleTracker = new HashMap<>();
    private static int throttleCounter = 0;

    @Shadow @Final private Connection connection;
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private static Component IGNORE_STATUS_REASON;

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void handleIntention(ClientIntentionPacket packet) {
        this.connection.banner$setHostName(packet.hostName + ":" + packet.port); // CraftBukkit  - set hostname
        switch (packet.intention()) {
            case LOGIN -> {
                this.connection.setClientboundProtocolAfterHandshake(ClientIntent.LOGIN);
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
                if (packet.protocolVersion() != SharedConstants.getCurrentVersion().getProtocolVersion()) {
                    MutableComponent component;
                    if (packet.protocolVersion() < 754) {
                        component = Component.translatable("multiplayer.disconnect.outdated_client", new Object[]{SharedConstants.getCurrentVersion().getName()});
                    } else {
                        component = Component.translatable("multiplayer.disconnect.incompatible", new Object[]{SharedConstants.getCurrentVersion().getName()});
                    }

                    this.connection.send(new ClientboundLoginDisconnectPacket(component));
                    this.connection.disconnect(component);
                } else {
                    this.connection.setListener(new ServerLoginPacketListenerImpl(this.server, this.connection));
                }
            }
            case STATUS -> {
                ServerStatus serverStatus = this.server.getStatus();
                if (this.server.repliesToStatus() && serverStatus != null) {
                    this.connection.setClientboundProtocolAfterHandshake(ClientIntent.STATUS);
                    this.connection.setListener(new ServerStatusPacketListenerImpl(serverStatus, this.connection));
                } else {
                    this.connection.disconnect(IGNORE_STATUS_REASON);
                }
            }
            default -> throw new UnsupportedOperationException("Invalid intention " + packet.intention());
        }

    }
}
