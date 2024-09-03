package com.mohistmc.banner.injection.world.entity.projectile;

public interface InjectionAbstractHurtingProjectile {

    default void setDirection(double d3, double d4, double d5) {
        throw new IllegalStateException("Not implemented");
    }

    default float bridge$bukkitYield() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$isIncendiary() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setBukkitYield(float yield) {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setIsIncendiary(boolean incendiary) {
        throw new IllegalStateException("Not implemented");
    }
}
