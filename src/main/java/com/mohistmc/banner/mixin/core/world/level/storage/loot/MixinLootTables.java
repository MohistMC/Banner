package com.mohistmc.banner.mixin.core.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mohistmc.banner.injection.world.level.storage.loot.InjectionLootTables;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(LootTables.class)
public class MixinLootTables implements InjectionLootTables {

    @Shadow private Map<ResourceLocation, LootTable> tables;
    public Map<LootTable, ResourceLocation> lootTableToKey = ImmutableMap.of(); // CraftBukkit

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("RETURN"))
    private void banner$buildRev(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        Map<LootTable, ResourceLocation> lootTableToKeyBuilder = new HashMap<>();
        this.tables.forEach((lootTable, key) -> lootTableToKeyBuilder.put(key, lootTable));
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
