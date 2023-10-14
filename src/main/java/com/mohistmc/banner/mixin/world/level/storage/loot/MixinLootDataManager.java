package com.mohistmc.banner.mixin.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.mohistmc.banner.injection.world.level.storage.loot.InjectionLootDataManager;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootDataManager.class)
public class MixinLootDataManager implements InjectionLootDataManager {

    @Shadow private Map<LootDataId<?>, ?> elements;
    public Map<?, ResourceLocation> lootTableToKey = ImmutableMap.of(); // CraftBukkit

    @Inject(method = "apply", at = @At("RETURN"))
    private void banner$buildRev(Map<LootDataType<?>, Map<ResourceLocation, ?>> map, CallbackInfo ci) {
        // CraftBukkit start - build a reversed registry map
        ImmutableMap.Builder<Object, ResourceLocation> lootTableToKeyBuilder = ImmutableMap.builder();
        this.elements.forEach((key, lootTable) -> lootTableToKeyBuilder.put((Object) lootTable, key.location()));
        this.lootTableToKey = lootTableToKeyBuilder.build();
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
