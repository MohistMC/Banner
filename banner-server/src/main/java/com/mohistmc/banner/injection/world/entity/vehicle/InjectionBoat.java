package com.mohistmc.banner.injection.world.entity.vehicle;

public interface InjectionBoat {

    default double bridge$maxSpeed() {
        return 0;
    }

    default void banner$setMaxSpeed(double maxSpeed) {
    }

    default double bridge$occupiedDeceleration() {
        return 0;
    }

    default void banner$setOccupiedDeceleration(double occupiedDeceleration) {
    }

    default double bridge$unoccupiedDeceleration() {
        return 0;
    }

    default void banner$setUnoccupiedDeceleration(double occupiedDeceleration) {
    }

    default boolean bridge$landBoats() {
        return false;
    }

    default void banner$setLandBoats(boolean landBoats) {
    }
}
