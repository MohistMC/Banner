package com.mohistmc.banner.bukkit;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;

public class DiscardedPayloadCodec {

    public static <T extends FriendlyByteBuf> StreamCodec<T, DiscardedPayload> codec(ResourceLocation resourceLocation, int i) {
        return CustomPacketPayload.codec((discardedPayload, friendlyByteBuf) -> {
            var data = discardedPayload.bridge$getData();
            if (data != null) {
                friendlyByteBuf.writeBytes(data);
            }
        }, (friendlyByteBuf) -> {
            int j = friendlyByteBuf.readableBytes();
            if (j >= 0 && j <= i) {
                var payload = new DiscardedPayload(resourceLocation);
                payload.bridge$setData(friendlyByteBuf.readBytes(j));
                return payload;
            } else {
                throw new IllegalArgumentException("Payload may not be larger than " + i + " bytes");
            }
        });
    }
}