package com.mohistmc.banner.mixin.world.entity.ai.goal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.SkeletonTrapGoal;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkeletonTrapGoal.class)
public class MixinSkeletonTrapGoal {


    // @formatter:off
    @Shadow @Final private SkeletonHorse horse;
    // @formatter:on

    @Inject(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"),
            slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/SkeletonTrapGoal;createHorse(Lnet/minecraft/world/DifficultyInstance;)Lnet/minecraft/world/entity/animal/horse/AbstractHorse;")))
    private void banner$thunder(CallbackInfo ci) {
        this.horse.level().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.TRAP);
    }

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$addHorse(ServerLevel world, Entity entityIn) {
        world.strikeLightning((LightningBolt) entityIn, LightningStrikeEvent.Cause.TRAP);
        return true;
    }

    @Inject(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/SkeletonTrapGoal;createHorse(Lnet/minecraft/world/DifficultyInstance;)Lnet/minecraft/world/entity/animal/horse/AbstractHorse;")))
    private void banner$jockey(CallbackInfo ci) {
         this.horse.level().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.JOCKEY);
    }
}
