package com.mohistmc.banner.bukkit.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class BannerServerboundCustomPayloadPacket {

    private static UnknownPayload readUnknownPayload(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
        int i = friendlyByteBuf.readableBytes();
        if (i >= 0 && i <= 32767) {
            // CraftBukkit start
            return new UnknownPayload(resourceLocation, friendlyByteBuf.readBytes(i));
            // CraftBukkit end
        } else {
            throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
        }
    }

    // CraftBukkit start
    public record UnknownPayload(ResourceLocation id, io.netty.buffer.ByteBuf data) implements CustomPacketPayload {

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeBytes(data);
        }
    }
    // CraftBukkit end
}
