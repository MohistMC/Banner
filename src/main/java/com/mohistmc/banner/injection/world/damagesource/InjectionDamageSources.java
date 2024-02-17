package com.mohistmc.banner.injection.world.damagesource;

import net.minecraft.world.damagesource.DamageSource;

public interface InjectionDamageSources {

    default DamageSource poison() {
        return null;
    }

    default DamageSource melting() {
        return null;
    }

    default DamageSource bridge$melting() {
        return null;
    }

    default DamageSource bridge$poison() {
        return null;
    }
}
