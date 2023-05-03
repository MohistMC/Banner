package com.mohistmc.banner.mixin.world.entity.animal.horse;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkeletonTrapGoal.class)
public class MixinSkeletonTrapGoal {

    @Shadow @Final private SkeletonHorse horse;
    private ServerLevel banner$serverLevel = (ServerLevel)this.horse.level;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", ordinal = 0))
    private boolean banner$striking(ServerLevel instance, Entity entity) {
        return instance.strikeLightning(entity, LightningStrikeEvent.Cause.TRAP);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V", ordinal = 0))
    private void banner$pushReason0(CallbackInfo ci) {
        this.banner$serverLevel.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.TRAP);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V", ordinal = 1))
    private void banner$pushReason1(CallbackInfo ci) {
        this.banner$serverLevel.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.JOCKEY);
    }
}
