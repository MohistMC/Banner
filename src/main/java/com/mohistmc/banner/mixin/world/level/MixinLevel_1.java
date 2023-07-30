package com.mohistmc.banner.mixin.world.level;

import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world/level.Level$1")
public class MixinLevel_1 {

    @Redirect(method = "getCenterX",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/border/WorldBorder;getCenterX()D"))
    private double banner$resetBorderX(WorldBorder instance) {
        return instance.getCenterX(); // CraftBukkit
    }

    @Redirect(method = "getCenterZ",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;getCenterZ()D"))
    private double banner$resetBorderZ(WorldBorder instance) {
        return instance.getCenterZ(); // CraftBukkit
    }
}
