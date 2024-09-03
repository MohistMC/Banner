package com.mohistmc.banner.mixin;

import net.minecraft.CrashReport;
import net.minecraft.SystemReport;
import org.bukkit.craftbukkit.CraftCrashReport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
public abstract class MixinCrashReport {

    @Shadow @Final private SystemReport systemReport;

    @Mutable @Shadow @Final private Throwable exception;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void banner$init(String string, Throwable throwable, CallbackInfo ci) {
        this.systemReport.setDetail("CraftBukkit Information", new CraftCrashReport()); // CraftBukkit
    }
}
