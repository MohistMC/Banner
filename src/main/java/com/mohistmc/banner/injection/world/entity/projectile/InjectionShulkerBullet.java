package com.mohistmc.banner.injection.world.entity.projectile;

import net.minecraft.world.entity.Entity;

public interface InjectionShulkerBullet {

    default Entity getTarget() {
        return null;
    }

    default void setTarget(Entity e) {

    }
}
