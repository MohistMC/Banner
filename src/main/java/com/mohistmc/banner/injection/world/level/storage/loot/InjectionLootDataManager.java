package com.mohistmc.banner.injection.world.level.storage.loot;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

public interface InjectionLootDataManager {

    default Map<?, ResourceLocation> bridge$lootTableToKey() {
        return null;
    }

    default void banner$setLootTableToKey(Map<LootTable, ResourceLocation> lootTableToKey) {
    }
}
