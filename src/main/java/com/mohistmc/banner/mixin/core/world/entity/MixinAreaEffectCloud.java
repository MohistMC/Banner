package com.mohistmc.banner.mixin.core.world.entity;

import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;

import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(AreaEffectCloud.class)
public abstract class MixinAreaEffectCloud extends Entity implements TraceableEntity {

    @Shadow public abstract boolean isWaiting();

    @Shadow public abstract float getRadius();

    @Shadow public abstract ParticleOptions getParticle();

    @Shadow public int waitTime;

    @Shadow private int duration;

    @Shadow protected abstract void setWaiting(boolean bl);

    @Shadow public float radiusPerTick;

    @Shadow public abstract void setRadius(float f);

    @Shadow @Final private Map<Entity, Integer> victims;

    @Shadow public PotionContents potionContents;

    @Shadow public int reapplicationDelay;

    @Shadow public float radiusOnUse;

    @Shadow public int durationOnUse;

    public MixinAreaEffectCloud(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    // TODO fix patches
}
