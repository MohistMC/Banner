package com.mohistmc.banner.injection.world.damagesource;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public interface InjectionDamageSource {

    default boolean isSweep(){
        throw new IllegalStateException("Not implemented");
    }

    default DamageSource sweep() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean isMelting() {
        throw new IllegalStateException("Not implemented");
    }

    default DamageSource melting() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean isPoison() {
        throw new IllegalStateException("Not implemented");
    }

    default DamageSource poison() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$sweep() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$melting() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$poison() {
        throw new IllegalStateException("Not implemented");
    }

    default Entity getCausingEntity() {
        throw new IllegalStateException("Not implemented");
    }

    default DamageSource customCausingEntity(Entity entity) {
        throw new IllegalStateException("Not implemented");
    }

    default org.bukkit.block.Block getDirectBlock() {
        throw new IllegalStateException("Not implemented");
    }

    default DamageSource directBlock(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos blockPosition) {
        throw new IllegalStateException("Not implemented");
    }

    default DamageSource directBlock(org.bukkit.block.Block block) {
        throw new IllegalStateException("Not implemented");
    }

    default DamageSource cloneInstance() {
        throw new IllegalStateException("Not implemented");
    }

    default org.bukkit.block.BlockState getDirectBlockState() {
        throw new IllegalStateException("Not implemented");
    }

    default DamageSource directBlockState(org.bukkit.block.BlockState blockState) {
        throw new IllegalStateException("Not implemented");
    }

    default Entity getDamager() {
        throw new IllegalStateException("Not implemented");
    }

    default Entity getCausingDamager() {
        throw new IllegalStateException("Not implemented");
    }

    default DamageSource customEntityDamager(Entity entity) {
        throw new IllegalStateException("Not implemented");
    }

    default DamageSource customCausingEntityDamager(Entity entity) {
        throw new IllegalStateException("Not implemented");
    }
}
