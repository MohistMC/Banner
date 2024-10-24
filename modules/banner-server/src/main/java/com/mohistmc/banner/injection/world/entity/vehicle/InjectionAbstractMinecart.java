package com.mohistmc.banner.injection.world.entity.vehicle;

import org.bukkit.util.Vector;

public interface InjectionAbstractMinecart {

    default double bridge$maxSpeed() {
        return 0;
    }

    default void banner$setMaxSpeed(double maxSpeed) {
    }

    default boolean bridge$slowWhenEmpty() {
        return false;
    }

    default void banner$setSlowWhenEmpty(boolean slowWhenEmpty) {

    }

    default Vector getFlyingVelocityMod() {
        return null;
    }

    default void setFlyingVelocityMod(Vector flying) {

    }

    default Vector getDerailedVelocityMod() {
        return null;
    }

    default void setDerailedVelocityMod(Vector derailed) {
    }
}
