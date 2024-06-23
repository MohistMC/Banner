package com.mohistmc.banner.injection.world.entity.vehicle;

public interface InjectionBoat {

    default double bridge$maxSpeed() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setMaxSpeed(double maxSpeed) {
        throw new IllegalStateException("Not implemented");
    }

    default double bridge$occupiedDeceleration() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setOccupiedDeceleration(double occupiedDeceleration) {
        throw new IllegalStateException("Not implemented");
    }

    default double bridge$unoccupiedDeceleration() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setUnoccupiedDeceleration(double occupiedDeceleration) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$landBoats() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setLandBoats(boolean landBoats) {
        throw new IllegalStateException("Not implemented");
    }
}
