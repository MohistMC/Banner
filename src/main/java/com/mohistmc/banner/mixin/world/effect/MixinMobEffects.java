package com.mohistmc.banner.mixin.world.effect;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import org.bukkit.craftbukkit.v1_19_R3.potion.CraftPotionEffectType;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MobEffects.class)
public class MixinMobEffects {

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    private static MobEffect register(int id, String key, MobEffect effect) {
        // CraftBukkit start
        effect = Registry.registerMapping(BuiltInRegistries.MOB_EFFECT, id, key, effect);
        PotionEffectType.registerPotionEffectType(new CraftPotionEffectType(effect));
        return effect;
    }
}
