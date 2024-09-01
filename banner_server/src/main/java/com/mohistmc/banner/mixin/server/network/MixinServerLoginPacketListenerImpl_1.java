package com.mohistmc.banner.mixin.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net.minecraft.server.network.ServerLoginPacketListenerImpl$1")
public class MixinServerLoginPacketListenerImpl_1 {

    @Shadow @Final
    ServerLoginPacketListenerImpl field_14176;

    @Inject(method = "run", at = @At(value = "INVOKE",
            target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
            remap = false, ordinal = 0), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$callPreLoginEvent(CallbackInfo ci, String string,
                                          ProfileResult profileResult,
                                          GameProfile gameProfile) {
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
