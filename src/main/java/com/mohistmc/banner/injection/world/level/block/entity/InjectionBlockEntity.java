package com.mohistmc.banner.injection.world.level.block.entity;

import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.inventory.InventoryHolder;

public interface InjectionBlockEntity {

    default CraftPersistentDataContainer bridge$persistentDataContainer() {
        return null;
    }

    default InventoryHolder bridge$getOwner() {
        return null;
    }
}
