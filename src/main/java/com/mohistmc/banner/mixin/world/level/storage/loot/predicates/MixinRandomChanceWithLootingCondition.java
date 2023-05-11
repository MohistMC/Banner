package com.mohistmc.banner.mixin.world.level.storage.loot.predicates;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LootItemRandomChanceWithLootingCondition.class)
public class MixinRandomChanceWithLootingCondition {

    @Shadow @Final
    float percent;
    @Shadow @Final
    float lootingMultiplier;

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public boolean test(LootContext lootContext) {
        Entity entity = (Entity)lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY);
        int i = 0;
        if (entity instanceof LivingEntity) {
            i = EnchantmentHelper.getMobLooting((LivingEntity)entity);
        }
        // CraftBukkit start - only use lootingModifier if set by Bukkit
        if (lootContext.hasParam(BukkitExtraConstants.LOOTING_MOD)) {
            i = lootContext.getParamOrNull(BukkitExtraConstants.LOOTING_MOD);
        }
        // CraftBukkit end

        return lootContext.getRandom().nextFloat() < this.percent + (float)i * this.lootingMultiplier;
    }
}

