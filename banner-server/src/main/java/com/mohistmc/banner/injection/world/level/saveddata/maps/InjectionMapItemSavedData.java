package com.mohistmc.banner.injection.world.level.saveddata.maps;

import org.bukkit.craftbukkit.map.CraftMapView;

import java.util.UUID;

public interface InjectionMapItemSavedData {

    default CraftMapView bridge$mapView() {
        throw new IllegalStateException("Not implemented");
    }

    default UUID bridge$uniqueId() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setUniqueId(UUID uuid) {
        throw new IllegalStateException("Not implemented");
    }

    default String bridge$id() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setId(String id) {
        throw new IllegalStateException("Not implemented");
    }
}
