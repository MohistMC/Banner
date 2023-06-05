package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.entity.monster.Ghast$GhastShootFireballGoal")
public abstract class MixinGhast_GhastShootFireballGoal {

    @Shadow @Final private Ghast ghast;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$setYaw(Level world, Entity entityIn) {
        ((LargeFireball) entityIn).banner$setBukkitYield(this.ghast.getExplosionPower());
        return world.addFreshEntity(entityIn);
    }
}
