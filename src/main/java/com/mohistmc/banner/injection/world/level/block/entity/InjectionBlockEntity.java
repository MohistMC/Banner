package com.mohistmc.banner.injection.world.level.block.entity;

import org.bukkit.craftbukkit.v1_19_R3.persistence.CraftPersistentDataContainer;
import org.bukkit.inventory.InventoryHolder;

public interface InjectionBlockEntity {

    default CraftPersistentDataContainer bridge$persistentDataContainer() {
        return null;
    }

    default void banner$setPersistentDataContainer(CraftPersistentDataContainer persistentDataContainer) {
    }

    default InventoryHolder getOwner() {
        return null;
    }
}
