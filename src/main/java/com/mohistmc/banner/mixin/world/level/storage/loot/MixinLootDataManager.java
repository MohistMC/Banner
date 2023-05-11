package com.mohistmc.banner.mixin.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.mohistmc.banner.injection.world.level.storage.loot.InjectionLootDataManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(LootDataManager.class)
public class MixinLootDataManager implements InjectionLootDataManager {

    public Map<LootTable, ResourceLocation> lootTableToKey = ImmutableMap.of(); // CraftBukkit

    @Inject(method = "apply", at = @At("RETURN"))
    private void banner$buildRev(Map<LootDataType<?>, Map<ResourceLocation, ?>> map, CallbackInfo ci) {
        Map<LootTable, ResourceLocation> lootTableToKeyBuilder = new HashMap<>();
        //this.lootTableToKey.forEach((lootTable, key) -> lootTableToKeyBuilder.put(key, lootTable)); Banner - TODO
        this.lootTableToKey = ImmutableMap.copyOf(lootTableToKeyBuilder);
    }

    @Override
    public Map<LootTable, ResourceLocation> bridge$lootTableToKey() {
        return lootTableToKey;
    }

    @Override
    public void banner$setLootTableToKey(Map<LootTable, ResourceLocation> lootTableToKey) {
        this.lootTableToKey = lootTableToKey;
    }
}
