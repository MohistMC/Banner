package com.mohistmc.banner.injection.world.damagesource;

import net.minecraft.world.damagesource.DamageSource;

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
}
