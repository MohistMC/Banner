package com.mohistmc.banner.mixin.server.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.LegacyQueryHandler;
import net.minecraft.server.network.ServerConnectionListener;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Mixin(LegacyQueryHandler.class)
public abstract class MixinLegacyQueryHandler {

    @Shadow @Final private ServerConnectionListener serverConnectionListener;

    @Shadow @Final private static Logger LOGGER;

    @Shadow protected abstract void sendFlushAndClose(ChannelHandlerContext ctx, ByteBuf data);

    @Shadow protected abstract ByteBuf createReply(String string);

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
            if (bytebuf.readUnsignedByte() != 254) {
                return;
            }

            InetSocketAddress inetsocketaddress = (InetSocketAddress) channelhandlercontext.channel().remoteAddress();
            MinecraftServer minecraftserver = this.serverConnectionListener.getServer();
            int i = bytebuf.readableBytes();
            String s;
            org.bukkit.event.server.ServerListPingEvent event = CraftEventFactory.callServerListPingEvent(minecraftserver.bridge$server(), inetsocketaddress.getAddress(), minecraftserver.getMotd(), minecraftserver.getPlayerCount(), minecraftserver.getMaxPlayers()); // CraftBukkit

            switch (i) {
                case 0:
                    LOGGER.debug("Ping: (<1.3.x) from {}:{}", inetsocketaddress.getAddress(), inetsocketaddress.getPort());
                    s = String.format(Locale.ROOT, "%s\u00a7%d\u00a7%d", event.getMotd(), event.getNumPlayers(), event.getMaxPlayers()); // CraftBukkit
                    this.sendFlushAndClose(channelhandlercontext, this.createReply(s));
                    break;
                case 1:
                    if (bytebuf.readUnsignedByte() != 1) {
                        return;
                    }

                    LOGGER.debug("Ping: (1.4-1.5.x) from {}:{}", inetsocketaddress.getAddress(), inetsocketaddress.getPort());
                    s = String.format(Locale.ROOT, "\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, minecraftserver.getServerVersion(), event.getMotd(), event.getNumPlayers(), event.getMaxPlayers()); // CraftBukkit
                    this.sendFlushAndClose(channelhandlercontext, this.createReply(s));
                    break;
                default:
                    boolean flag1 = bytebuf.readUnsignedByte() == 1;

                    flag1 &= bytebuf.readUnsignedByte() == 250;
                    flag1 &= "MC|PingHost".equals(new String(bytebuf.readBytes(bytebuf.readShort() * 2).array(), StandardCharsets.UTF_16BE));
                    int j = bytebuf.readUnsignedShort();

                    flag1 &= bytebuf.readUnsignedByte() >= 73;
                    flag1 &= 3 + bytebuf.readBytes(bytebuf.readShort() * 2).array().length + 4 == j;
                    flag1 &= bytebuf.readInt() <= 65535;
                    flag1 &= bytebuf.readableBytes() == 0;
                    if (!flag1) {
                        return;
                    }

                    LOGGER.debug("Ping: (1.6) from {}:{}", inetsocketaddress.getAddress(), inetsocketaddress.getPort());
                    String s1 = String.format(Locale.ROOT, "\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, minecraftserver.getServerVersion(), event.getMotd(), event.getNumPlayers(), event.getMaxPlayers()); // CraftBukkit
                    ByteBuf bytebuf1 = this.createReply(s1);

                    try {
                        this.sendFlushAndClose(channelhandlercontext, bytebuf1);
                    } finally {
                        bytebuf1.release();
                    }
            }

            bytebuf.release();
            flag = false;
        } catch (RuntimeException runtimeexception) {
            ;
        } finally {
            if (flag) {
                bytebuf.resetReaderIndex();
                channelhandlercontext.channel().pipeline().remove("legacy_query");
                channelhandlercontext.fireChannelRead(object);
            }

        }
    }
}
