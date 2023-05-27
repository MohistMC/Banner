package com.mohistmc.banner.mixin.core.server.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.LegacyQueryHandler;
import net.minecraft.server.network.ServerConnectionListener;
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
     * @reason
     */
    @Overwrite
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) {
        ByteBuf byteBuf = (ByteBuf)object;
        byteBuf.markReaderIndex();
        boolean bl = true;

        try {
            try {
                if (byteBuf.readUnsignedByte() != 254) {
                    return;
                }

                InetSocketAddress inetSocketAddress = (InetSocketAddress)channelHandlerContext.channel().remoteAddress();
                MinecraftServer minecraftServer = this.serverConnectionListener.getServer();
                int i = byteBuf.readableBytes();
                String string;
                org.bukkit.event.server.ServerListPingEvent event = org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory.callServerListPingEvent(minecraftServer.bridge$server(), inetSocketAddress.getAddress(), minecraftServer.getMotd(), minecraftServer.getPlayerCount(), minecraftServer.getMaxPlayers()); // CraftBukkit
                switch (i) {
                    case 0 -> {
                        LOGGER.debug("Ping: (<1.3.x) from {}:{}", inetSocketAddress.getAddress(), inetSocketAddress.getPort());
                        string = String.format(Locale.ROOT, "%s§%d§%d", event.getMotd(), event.getNumPlayers(), event.getMaxPlayers()); // CraftBukkit
                        this.sendFlushAndClose(channelHandlerContext, this.createReply(string));
                    }
                    case 1 -> {
                        if (byteBuf.readUnsignedByte() != 1) {
                            return;
                        }
                        LOGGER.debug("Ping: (1.4-1.5.x) from {}:{}", inetSocketAddress.getAddress(), inetSocketAddress.getPort());
                        string = String.format(Locale.ROOT, "§1\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, minecraftServer.getServerVersion(), event.getMotd(), event.getNumPlayers(), event.getMaxPlayers()); // CraftBukkit
                        this.sendFlushAndClose(channelHandlerContext, this.createReply(string));
                    }
                    default -> {
                        boolean bl2 = byteBuf.readUnsignedByte() == 1;
                        bl2 &= byteBuf.readUnsignedByte() == 250;
                        bl2 &= "MC|PingHost".equals(new String(byteBuf.readBytes(byteBuf.readShort() * 2).array(), StandardCharsets.UTF_16BE));
                        int j = byteBuf.readUnsignedShort();
                        bl2 &= byteBuf.readUnsignedByte() >= 73;
                        bl2 &= 3 + byteBuf.readBytes(byteBuf.readShort() * 2).array().length + 4 == j;
                        bl2 &= byteBuf.readInt() <= 65535;
                        bl2 &= byteBuf.readableBytes() == 0;
                        if (!bl2) {
                            return;
                        }
                        LOGGER.debug("Ping: (1.6) from {}:{}", inetSocketAddress.getAddress(), inetSocketAddress.getPort());
                        String string2 = String.format(Locale.ROOT, "§1\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, minecraftServer.getServerVersion(), event.getMotd(), event.getNumPlayers(), event.getMaxPlayers()); // CraftBukkit
                        ByteBuf byteBuf2 = this.createReply(string2);
                        try {
                            this.sendFlushAndClose(channelHandlerContext, byteBuf2);
                        } finally {
                            byteBuf2.release();
                        }
                    }
                }

                byteBuf.release();
                bl = false;
            } catch (RuntimeException var21) {
            }

        } finally {
            if (bl) {
                byteBuf.resetReaderIndex();
                channelHandlerContext.channel().pipeline().remove("legacy_query");
                channelHandlerContext.fireChannelRead(object);
            }

        }
    }
}
