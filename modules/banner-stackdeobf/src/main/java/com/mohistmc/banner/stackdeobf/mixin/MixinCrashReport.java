package com.mohistmc.banner.stackdeobf.mixin;

import com.mohistmc.banner.stackdeobf.mappings.RemappedThrowable;
import com.mohistmc.banner.stackdeobf.mappings.RemappingUtil;
import net.minecraft.CrashReport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrashReport.class)
public class MixinCrashReport {


    @Mutable
    @Shadow
    @Final
    private Throwable exception;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void banner$postInit(String title, Throwable throwable, CallbackInfo ci) {
        this.exception = RemappingUtil.remapThrowable(throwable);
    }

    @Inject(method = "getException", at = @At("HEAD"), cancellable = true)
    public void banner$preExceptionGet(CallbackInfoReturnable<Throwable> cir) {
        // redirect calls to getException to the original, unmapped Throwable
        //
        // this method is called in the ReportedException, which
        // caused the "RemappedThrowable" name to show up in the logger

        if (this.exception instanceof RemappedThrowable remapped) {
            cir.setReturnValue(remapped.getOriginal());
        }
    }
}
