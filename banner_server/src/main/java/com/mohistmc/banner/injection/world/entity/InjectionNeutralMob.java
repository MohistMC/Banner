package com.mohistmc.banner.injection.world.entity;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface InjectionNeutralMob {

    default boolean setTarget(@Nullable LivingEntity entityliving, org.bukkit.event.entity.EntityTargetEvent.TargetReason reason, boolean fireEvent) { // CraftBukkit
        throw new IllegalStateException("Not implemented");
    }
}
