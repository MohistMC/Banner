package com.mohistmc.banner.injection.world.damagesource;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public interface InjectionDamageSource {

    default boolean isSweep(){
        return false;
    }

    default DamageSource sweep() {
        return null;
    }

    default boolean isMelting() {
        return false;
    }

    default DamageSource melting() {
        return null;
    }

    default boolean isPoison() {
        return false;
    }

    default DamageSource poison() {
        return null;
    }

    default boolean bridge$sweep() {
        return false;
    }

    default boolean bridge$melting() {
        return false;
    }

    default boolean bridge$poison() {
        return false;
    }

    default Entity getCausingEntity() {
        return null;
    }

    default DamageSource customCausingEntity(Entity entity) {
        return null;
    }

    default org.bukkit.block.Block getDirectBlock() {
        return null;
    }

    default DamageSource directBlock(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos blockPosition) {
        return null;
    }

    default DamageSource directBlock(org.bukkit.block.Block block) {
        return null;
    }

    default DamageSource cloneInstance() {
        return null;
    }
}
