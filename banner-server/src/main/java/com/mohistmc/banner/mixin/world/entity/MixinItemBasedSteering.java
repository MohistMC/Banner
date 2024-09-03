package com.mohistmc.banner.mixin.world.entity;

import com.mohistmc.banner.injection.world.entity.InjectionItemBasedSteering;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.ItemBasedSteering;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemBasedSteering.class)
public abstract class MixinItemBasedSteering implements InjectionItemBasedSteering {

    @Shadow public boolean boosting;

    @Shadow public int boostTime;

    @Shadow @Final private SynchedEntityData entityData;

    @Shadow @Final private EntityDataAccessor<Integer> boostTimeAccessor;

    // CraftBukkit add setBoostTicks(int)
    @Override
    public void setBoostTicks(int ticks) {
        this.boosting = true;
        this.boostTime = 0;
        this.entityData.set(this.boostTimeAccessor, ticks);
    }
    // CraftBukkit end

}
