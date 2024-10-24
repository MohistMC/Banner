package com.mohistmc.banner.mixin.server.network;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.network.ServerLoginPacketListenerImpl$1")
public class MixinServerLoginPacketListenerImpl_1 {

    @Shadow @Final
    ServerLoginPacketListenerImpl field_14176;

    @Inject(method = "run", at = @At(value = "INVOKE",
            target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
            remap = false, ordinal = 0), cancellable = true)
    private void banner$callPreLoginEvent(CallbackInfo ci,
                                          @Local GameProfile gameProfile) {
        // CraftBukkit start - fire PlayerPreLoginEvent
        if (!field_14176.connection.isConnected()) {
            ci.cancel();
        }
        try {
            field_14176.callPlayerPreLoginEvents(gameProfile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // CraftBukkit end
    }
}
