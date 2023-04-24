package com.mohistmc.banner.mixin.world.entity.projectile;

import com.mohistmc.banner.injection.world.entity.projectile.InjectionAbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractHurtingProjectile.class)
public class MixinAbstractHurtingProjectile implements InjectionAbstractHurtingProjectile {

    public float bukkitYield = 1; // CraftBukkit
    public boolean isIncendiary = true; // CraftBukkit

    @Override
    public float bridge$bukkitYield() {
        return bukkitYield;
    }

    @Override
    public boolean bridge$isIncendiary() {
        return isIncendiary;
    }

    @Override
    public void banner$setBukkitYield(float yield) {
        bukkitYield = yield;
    }

    @Override
    public void banner$setIsIncendiary(boolean incendiary) {
        isIncendiary = incendiary;
    }
}
