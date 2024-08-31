package com.mohistmc.banner.bukkit;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;

public record BukkitDiscardedPayload(ResourceLocation id, io.netty.buffer.ByteBuf data) implements CustomPacketPayload {

    public BukkitDiscardedPayload(ResourceLocation id, io.netty.buffer.ByteBuf data) {
        this.id = id;
        this.data =data;
    }

    public static <T extends FriendlyByteBuf> StreamCodec<T, BukkitDiscardedPayload> codec(ResourceLocation resourceLocation, int i) {
        return CustomPacketPayload.codec((discardedPayload, friendlyByteBuf) -> {
            friendlyByteBuf.writeBytes(discardedPayload.data); // CraftBukkit - serialize
        }, (friendlyByteBuf) -> {
            int j = friendlyByteBuf.readableBytes();
            if (j >= 0 && j <= i) {
                return new BukkitDiscardedPayload(resourceLocation, friendlyByteBuf.readBytes(j));
            } else {
                throw new IllegalArgumentException("Payload may not be larger than " + i + " bytes");
            }
        });
    }

    public CustomPacketPayload.Type<DiscardedPayload> type() {
        return new CustomPacketPayload.Type(this.id);
    }

    public ResourceLocation id() {
        return this.id;
    }
}
