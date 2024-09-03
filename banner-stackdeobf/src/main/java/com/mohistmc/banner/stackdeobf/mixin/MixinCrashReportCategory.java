package com.mohistmc.banner.stackdeobf.mixin;

import com.mohistmc.banner.stackdeobf.mappings.RemappingUtil;
import net.minecraft.CrashReportCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrashReportCategory.class)
public class MixinCrashReportCategory {

    @Shadow
    private StackTraceElement[] stackTrace;

    @Inject(method = "fillInStackTrace", at = @At(value = "INVOKE", target = "Ljava/lang/System;arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V", shift = At.Shift.AFTER))
    public void banner$postStackTraceFill(int i, CallbackInfoReturnable<Integer> cir) {
        RemappingUtil.remapStackTraceElements(this.stackTrace);
    }
}