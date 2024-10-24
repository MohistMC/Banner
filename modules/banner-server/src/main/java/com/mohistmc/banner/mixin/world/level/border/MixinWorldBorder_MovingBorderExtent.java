package com.mohistmc.banner.mixin.world.level.border;

import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.level.border.WorldBorder.MovingBorderExtent")
public abstract class MixinWorldBorder_MovingBorderExtent {

    @Shadow public abstract long getLerpRemainingTime();

    @SuppressWarnings("target")
    @Shadow(aliases = {"field_12743"}, remap = false)
    @Final
    WorldBorder outerThis;

    @Shadow @Final private double from;

    @Shadow @Final private double to;

    @Shadow @Final private double lerpDuration;

    @Inject(method = "update", at = @At("HEAD"))
    private void banner$borderEvent(CallbackInfoReturnable<WorldBorder.BorderExtent> cir) {
        if (outerThis.bridge$world() != null && this.getLerpRemainingTime() <= 0L) new io.papermc.paper.event.world.border.WorldBorderBoundsChangeFinishEvent(outerThis.bridge$world().getWorld(), outerThis.bridge$world().getWorld().getWorldBorder(), this.from, this.to, this.lerpDuration).callEvent(); // Paper
    }
}
