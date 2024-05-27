package com.mohistmc.banner.injection.world.level.block.entity;

import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.inventory.InventoryHolder;

public interface InjectionBlockEntity {

    default CraftPersistentDataContainer bridge$persistentDataContainer() {
        return null;
    }

    default InventoryHolder bridge$getOwner() {
        return null;
    }

    default void setPatterns(BannerPatternLayers bannerPatternLayers) {

    }
}
