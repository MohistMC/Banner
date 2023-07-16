package com.mohistmc.banner.mixin.world.level.storage.loot.predicates;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ExplosionCondition.class)
public class MixinExplosionCondition {

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public boolean test(LootContext lootContext) {
        Float float_ = (Float)lootContext.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
        if (float_ != null) {
            RandomSource randomSource = lootContext.getRandom();
            float f = 1.0F / float_;
            // CraftBukkit - <= to < to allow for plugins to completely disable block drops from explosions
            return randomSource.nextFloat() < f;
        } else {
            return true;
        }
    }
}
