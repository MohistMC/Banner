package com.mohistmc.banner.injection.world.entity.projectile;

import net.minecraft.world.entity.Entity;

public interface InjectionShulkerBullet {

    default Entity getTarget() {
        throw new IllegalStateException("Not implemented");
    }

    default void setTarget(Entity e) {
        throw new IllegalStateException("Not implemented");
    }
}
