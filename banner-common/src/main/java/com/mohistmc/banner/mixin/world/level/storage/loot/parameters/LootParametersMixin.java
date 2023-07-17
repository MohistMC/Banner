package com.mohistmc.banner.mixin.world.level.storage.loot.parameters;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LootContextParams.class)
public class LootParametersMixin {

    private static final LootContextParam<Integer> LOOTING_MOD = BukkitExtraConstants.LOOTING_MOD;
}
