package com.mohistmc.banner.injection.world.level.storage.loot;

import net.minecraft.world.Container;
import net.minecraft.world.level.storage.loot.LootContext;

public interface InjectionLootTable {

    default void fillInventory(Container iinventory, LootContext loottableinfo, boolean plugin) {
    }
}
