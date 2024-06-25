package com.mohistmc.banner.injection.world.damagesource;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface InjectionDamageSources {

    default DamageSource melting() {
        throw new IllegalStateException("Not implemented");
    }

    default DamageSource poison() {
        throw new IllegalStateException("Not implemented");
    }

    default DamageSource explosion(@Nullable Entity entity, @Nullable Entity entity1, ResourceKey<DamageType> resourceKey) {
        throw new IllegalStateException("Not implemented");
    }

    default DamageSource badRespawnPointExplosion(Vec3 vec3d, org.bukkit.block.BlockState blockState) {
        throw new IllegalStateException("Not implemented");
    }
}
