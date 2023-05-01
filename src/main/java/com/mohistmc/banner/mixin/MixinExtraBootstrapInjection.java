package com.mohistmc.banner.mixin;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import io.izzel.arclight.mixin.injector.EjectorInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;

@Mixin(value = MixinExtrasBootstrap.class, remap = false)
public class MixinExtraBootstrapInjection {

    @Inject(method = "initialize",
            at=  @At(value = "INVOKE",
                    target = "Lorg/spongepowered/asm/mixin/injection/struct/InjectionInfo;register(Ljava/lang/Class;)V",
            ordinal = 4,
            shift = At.Shift.AFTER))
    private static void banner$addInit(boolean runtime, CallbackInfo ci) {
        InjectionInfo.register(EjectorInfo.class);
    }
}
