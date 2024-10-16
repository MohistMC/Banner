package com.mohistmc.banner.injection.world.damagesource;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.bukkit.block.Block;

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

    default Block getDirectBlock() {
        return null;
    }


    default Entity bridge$getCausingEntity() {
        return null;
    }

    default DamageSource bridge$customCausingEntity(Entity entity) {
        return null;
    }

    default DamageSource bridge$setCustomCausingEntity(Entity entity) {
        return null;
    }

    default Block bridge$directBlock() {
        return null;
    }

    default DamageSource bridge$directBlock(Block block) {
        return null;
    }

    default DamageSource bridge$setDirectBlock(Block block) {
        return null;
    }

    default DamageSource cloneInstance() {
        return null;
    }
}
