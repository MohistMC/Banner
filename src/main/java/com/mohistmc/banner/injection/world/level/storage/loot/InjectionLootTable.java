package com.mohistmc.banner.injection.world.level.storage.loot;

import net.minecraft.world.Container;
import net.minecraft.world.level.storage.loot.LootParams;
import org.bukkit.craftbukkit.CraftLootTable;

public interface InjectionLootTable {

    default void fillInventory(Container iinventory, LootParams lootparams, long i, boolean plugin) {
    }

    default CraftLootTable bridge$craftLootTable() {
        return null;
    }

    default void banner$setCraftLootTable(CraftLootTable craftLootTable) {
    }
}
