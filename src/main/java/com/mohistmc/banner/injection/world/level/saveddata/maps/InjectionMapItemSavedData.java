package com.mohistmc.banner.injection.world.level.saveddata.maps;

import org.bukkit.craftbukkit.v1_19_R3.map.CraftMapView;

import java.util.UUID;

public interface InjectionMapItemSavedData {

    default CraftMapView bridge$mapView() {
        return null;
    }

    default UUID bridge$uniqueId() {
        return null;
    }

    default void banner$setUniqueId(UUID uuid) {
    }

    default String bridge$id() {
        return null;
    }
}
