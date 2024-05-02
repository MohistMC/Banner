package com.mohistmc.banner.mixin.core.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.mohistmc.banner.injection.world.level.storage.loot.InjectionLootDataManager;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import org.bukkit.craftbukkit.CraftLootTable;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LootDataManager.class)
public class MixinLootDataManager implements InjectionLootDataManager {

    public Map<?, ResourceLocation> lootTableToKey = ImmutableMap.of(); // CraftBukkit

    @Inject(method = "apply", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$buildRev(Map<LootDataType<?>, Map<ResourceLocation, ?>> map,
                                 CallbackInfo ci, Object object, ImmutableMap.Builder<LootDataId<?>, Object> builder,
                                 ImmutableMultimap.Builder<LootDataType<?>, ResourceLocation> builder2, ProblemReporter.Collector collector,
                                 Map<LootDataId<?>, ?> map2, ValidationContext validationContext) {
        // CraftBukkit start
        map2.forEach((key, lootTable) -> {
            if (object instanceof LootTable table) {
                table.banner$setCraftLootTable(new CraftLootTable(CraftNamespacedKey.fromMinecraft(key.location()), table));
            }
        });
        // CraftBukkit end
    }

    @Override
    public Map<?, ResourceLocation> bridge$lootTableToKey() {
        return lootTableToKey;
    }

    @Override
    public void banner$setLootTableToKey(Map<LootTable, ResourceLocation> lootTableToKey) {
        this.lootTableToKey = lootTableToKey;
    }
}
