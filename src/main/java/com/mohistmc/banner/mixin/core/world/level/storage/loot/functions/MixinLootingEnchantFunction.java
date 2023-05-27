package com.mohistmc.banner.mixin.core.world.level.storage.loot.functions;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LootingEnchantFunction.class)
public abstract class MixinLootingEnchantFunction {

    // @formatter:off
    @Shadow @Final NumberProvider value;
    @Shadow @Final int limit;
    @Shadow abstract boolean hasLimit();
    // @formatter:on
    public ItemStack run(ItemStack stack, LootContext context) {
        Entity entity = (Entity)context.getParamOrNull(LootContextParams.KILLER_ENTITY);
        if (entity instanceof LivingEntity) {
            int i = EnchantmentHelper.getMobLooting((LivingEntity)entity);
            // CraftBukkit start - use lootingModifier if set by plugin
            if (context.hasParam(BukkitExtraConstants.LOOTING_MOD)) {
                i = context.getParamOrNull(BukkitExtraConstants.LOOTING_MOD);
                // CraftBukkit end
            }
            if (i <= 0) { // CraftBukkit - account for possible negative looting values from Bukkit
                return stack;
            }

            float f = (float)i * this.value.getFloat(context);
            stack.grow(Math.round(f));
            if (this.hasLimit() && stack.getCount() > this.limit) {
                stack.setCount(this.limit);
            }
        }

        return stack;
    }
}
