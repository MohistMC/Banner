package com.mohistmc.banner.mixin.core.world.damagesource;

import com.mohistmc.banner.injection.world.damagesource.InjectionDamageSource;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DamageSource.class)
public class MixinDamageSource implements InjectionDamageSource {

    // CraftBukkit start
    private boolean sweep;
    private boolean melting;
    private boolean poison;

    @Override
    public boolean isSweep() {
        return sweep;
    }

    @Override
    public DamageSource sweep() {
        this.sweep = true;
        return ((DamageSource) (Object) this);
    }

    @Override
    public boolean isMelting() {
        return melting;
    }

    @Override
    public DamageSource melting() {
        this.melting = true;
        return ((DamageSource) (Object) this);
    }

    @Override
    public boolean isPoison() {
        return poison;
    }

    @Override
    public DamageSource poison() {
        this.poison = true;
        return ((DamageSource) (Object) this);
    }

    // CraftBukkit end

    // Banner start
    @Override
    public boolean bridge$sweep() {
        return sweep;
    }

    @Override
    public boolean bridge$melting() {
        return melting;
    }

    @Override
    public boolean bridge$poison() {
        return poison;
    }
    // Banner end
}
