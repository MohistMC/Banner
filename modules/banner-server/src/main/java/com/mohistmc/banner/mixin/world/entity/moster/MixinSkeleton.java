package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Skeleton.class)
public abstract class MixinSkeleton extends AbstractSkeleton {

    protected MixinSkeleton(EntityType<? extends AbstractSkeleton> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "doFreezeConversion", at = @At("HEAD"))
    private void banner$pushSkeletonReason(CallbackInfo ci) {
        this.bridge$pushTransformReason(EntityTransformEvent.TransformReason.FROZEN);
        this.level().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.FROZEN);
    }
}
