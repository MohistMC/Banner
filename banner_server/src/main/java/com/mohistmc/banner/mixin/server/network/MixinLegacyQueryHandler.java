package com.mohistmc.banner.mixin.server.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import java.net.SocketAddress;
import java.util.Locale;
import net.minecraft.server.ServerInfo;
import net.minecraft.server.network.LegacyQueryHandler;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LegacyQueryHandler.class)
public abstract class MixinLegacyQueryHandler {

    @Shadow @Final private static Logger LOGGER;


    @Shadow
    private static String createVersion0Response(ServerInfo serverInfo) {
        return null;
    }

    @Shadow @Final private ServerInfo server;

    @Shadow
    private static void sendFlushAndClose(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
    }

    @Shadow
    private static ByteBuf createLegacyDisconnectPacket(ByteBufAllocator byteBufAllocator, String string) {
        return null;
    }

    @Shadow
    private static boolean readCustomPayloadPacket(ByteBuf byteBuf) {
        return false;
    }

    @Shadow
    private static String createVersion1Response(ServerInfo serverInfo) {
        return null;
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void channelRead(ChannelHandlerContext channelhandlercontext, Object object) {
        ByteBuf bytebuf = (ByteBuf) object;

        bytebuf.markReaderIndex();
        boolean flag = true;

        try {
            try {
                if (bytebuf.readUnsignedByte() != 254) {
                    return;
                }

                SocketAddress socketaddress = channelhandlercontext.channel().remoteAddress();
                int i = bytebuf.readableBytes();
                String s;
                org.bukkit.event.server.ServerListPingEvent event = CraftEventFactory.callServerListPingEvent(socketaddress, server.getMotd(), server.getPlayerCount(), server.getMaxPlayers()); // CraftBukkit

                if (i == 0) {
                    LOGGER.debug("Ping: (<1.3.x) from {}", socketaddress);
                    s = createVersion0Response(this.server, event); // CraftBukkit
                    sendFlushAndClose(channelhandlercontext, createLegacyDisconnectPacket(channelhandlercontext.alloc(), s));
                } else {
                    if (bytebuf.readUnsignedByte() != 1) {
                        return;
                    }

                    if (bytebuf.isReadable()) {
                        if (!readCustomPayloadPacket(bytebuf)) {
                            return;
                        }

                        LOGGER.debug("Ping: (1.6) from {}", socketaddress);
                    } else {
                        LOGGER.debug("Ping: (1.4-1.5.x) from {}", socketaddress);
                    }

                    s = createVersion1Response(this.server, event); // CraftBukkit
                    sendFlushAndClose(channelhandlercontext, createLegacyDisconnectPacket(channelhandlercontext.alloc(), s));
                }

                bytebuf.release();
                flag = false;
            } catch (RuntimeException runtimeexception) {
                ;
            }

        } finally {
            if (flag) {
                bytebuf.resetReaderIndex();
                channelhandlercontext.channel().pipeline().remove(((LegacyQueryHandler) (Object) this));
                channelhandlercontext.fireChannelRead(object);
            }

        }
    }

    // CraftBukkit start
    private static String createVersion0Response(ServerInfo serverinfo, org.bukkit.event.server.ServerListPingEvent event) {
        return String.format(Locale.ROOT, "%s\u00a7%d\u00a7%d", event.getMotd(), event.getNumPlayers(), event.getMaxPlayers());
        // CraftBukkit end
    }

    // CraftBukkit start
    private static String createVersion1Response(ServerInfo serverinfo, org.bukkit.event.server.ServerListPingEvent event) {
        return String.format(Locale.ROOT, "\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, serverinfo.getServerVersion(), event.getMotd(), event.getNumPlayers(), event.getMaxPlayers());
        // CraftBukkit end
    }
}
