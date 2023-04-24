package com.mohistmc.banner.mixin.world.level.storage.loot;

import com.mohistmc.banner.injection.world.level.storage.loot.InjectionLootTable;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;

// TODO fix inject method
@Mixin(LootTable.class)
public class MixinLootTable implements InjectionLootTable {
}
