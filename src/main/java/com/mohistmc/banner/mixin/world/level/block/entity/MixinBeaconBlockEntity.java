package com.mohistmc.banner.mixin.world.level.block.entity;

import com.mohistmc.banner.injection.world.level.block.entity.InjectionBeaconBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.bukkit.craftbukkit.v1_19_R3.potion.CraftPotionUtil;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
public abstract class MixinBeaconBlockEntity implements InjectionBeaconBlockEntity {

    @Shadow public int levels;

    @Shadow @Nullable public MobEffect secondaryPower;

    @Shadow @Nullable public MobEffect primaryPower;

    @Inject(method = "load", at = @At("RETURN"))
    public void banner$level(CompoundTag tag, CallbackInfo ci) {
        this.levels = tag.getInt("Levels");
    }


    @Override
    public PotionEffect getPrimaryEffect() {
        return (this.primaryPower != null) ? CraftPotionUtil.toBukkit(new MobEffectInstance(this.primaryPower, this.getLevel(), this.getAmplification(), true, true)) : null;
    }

    @Override
    public PotionEffect getSecondaryEffect() {
        return (this.hasSecondaryEffect()) ? CraftPotionUtil.toBukkit(new MobEffectInstance(this.secondaryPower, getLevel(), getAmplification(), true, true)) : null;
    }

    @Override
    public boolean hasSecondaryEffect() {
        if (this.levels >= 4 && this.primaryPower != this.secondaryPower && this.secondaryPower != null) {
            return true;
        }
        return false;
    }

    @Override
    public byte getAmplification() {
        byte b0 = 0;
        if (this.levels >= 4 && this.primaryPower == this.secondaryPower) {
            b0 = 1;
        }
        return b0;
    }

    @Override
    public int getLevel() {
        int i = (9 + this.levels * 2) * 20;
        return i;
    }
}
