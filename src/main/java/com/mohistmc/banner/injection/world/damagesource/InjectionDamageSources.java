package com.mohistmc.banner.injection.world.damagesource;

import net.minecraft.world.damagesource.DamageSource;

public interface InjectionDamageSources {

    default DamageSource bridge$melting() {
        throw new IllegalStateException("Not implemented");
    }

    default DamageSource bridge$poison() {
        throw new IllegalStateException("Not implemented");
    }
}
