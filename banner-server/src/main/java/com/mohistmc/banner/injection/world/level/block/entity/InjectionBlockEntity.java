package com.mohistmc.banner.injection.world.level.block.entity;

import java.util.Set;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.inventory.InventoryHolder;

public interface InjectionBlockEntity {

    default CraftPersistentDataContainer bridge$persistentDataContainer() {
        throw new IllegalStateException("Not implemented");
    }

    default InventoryHolder bridge$getOwner() {
        throw new IllegalStateException("Not implemented");
    }

    default void setPatterns(BannerPatternLayers bannerPatternLayers) {
        throw new IllegalStateException("Not implemented");
    }

    default Set<DataComponentType<?>> applyComponentsSet(DataComponentMap datacomponentmap, DataComponentPatch datacomponentpatch) {
        return Set.of();
    }
}
