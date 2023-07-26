package com.mohistmc.banner.injection.world.entity.decoration;

import net.minecraft.world.phys.Vec3;

public interface InjectionArmorStand {

    default boolean bridge$canMove() {
        return false;
    }

    default void banner$setCanMove(boolean canMove) {

    }
}
