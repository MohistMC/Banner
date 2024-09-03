package com.mohistmc.banner.mixin.network.protocol;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketUtils.class)
public class MixinPacketUtils {

    @Inject(method = "method_11072", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/PacketListener;shouldHandleMessage(Lnet/minecraft/network/protocol/Packet;)Z"),
            cancellable = true)
    private static void banner$neverSync(PacketListener packetListener, Packet packet, CallbackInfo ci) {
        if (packetListener instanceof ServerCommonPacketListenerImpl serverCommonPacketListener && serverCommonPacketListener.bridge$processedDisconnect()) {
            ci.cancel();
        }
        // CraftBukkit - Don't handle sync packets for kicked players
    }
}
