package com.mohistmc.banner.injection.world.level.saveddata.maps;

import java.util.UUID;
import org.bukkit.craftbukkit.map.CraftMapView;

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

    default void banner$setId(String id) {
    }
}
