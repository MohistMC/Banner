package com.mohistmc.banner.mixin;

import com.mohistmc.banner.stackdeobf.mappings.RemappedThrowable;
import com.mohistmc.banner.stackdeobf.mappings.RemappingUtil;
import net.minecraft.CrashReport;
import net.minecraft.SystemReport;
import org.bukkit.craftbukkit.v1_19_R3.CraftCrashReport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrashReport.class)
public abstract class MixinCrashReport {

    @Shadow @Final private SystemReport systemReport;

    @Mutable @Shadow @Final private Throwable exception;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void banner$init(String string, Throwable throwable, CallbackInfo ci) {
        this.exception = RemappingUtil.remapThrowable(throwable);
        this.systemReport.setDetail("CraftBukkit Information", new CraftCrashReport()); // CraftBukkit
    }

    @Inject(method = "getException", at = @At("HEAD"), cancellable = true)
    public void preExceptionGet(CallbackInfoReturnable<Throwable> cir) {
        // redirect calls to getException to the original, unmapped Throwable
        //
        // this method is called in the ReportedException, which
        // caused the "RemappedThrowable" name to show up in the logger

        if (this.exception instanceof RemappedThrowable remapped) {
            cir.setReturnValue(remapped.getOriginal());
        }
    }
}
