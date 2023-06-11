package com.mohistmc.banner.injection.world.level.storage.loot;

import net.minecraft.world.Container;
import net.minecraft.world.level.storage.loot.LootParams;

public interface InjectionLootTable {

    default void fillInventory(Container iinventory, LootParams lootparams, long i, boolean plugin) {
    }
}
