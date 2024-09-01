package com.mohistmc.banner.mixin.network.protocol;

import com.mohistmc.banner.bukkit.DiscardedPayloadCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.bukkit.Bukkit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({ServerboundCustomPayloadPacket.class, ClientboundCustomPayloadPacket.class})
public class MixinCustomPayloadPacket {

    @ModifyArg(method = "<clinit>", require = 0, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;codec(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$FallbackProvider;Ljava/util/List;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static <B extends FriendlyByteBuf> CustomPacketPayload.FallbackProvider<B> banner$fallbackBukkit(CustomPacketPayload.FallbackProvider<B> arg) {
        return resourceLocation -> {
            if (Bukkit.getMessenger().getIncomingChannels().contains(resourceLocation.toString())
                    || Bukkit.getMessenger().getOutgoingChannels().contains(resourceLocation.toString())) {
                return DiscardedPayloadCodec.codec(resourceLocation, 32767);
            } else {
                return arg.create(resourceLocation);
            }
        };
    }
}
