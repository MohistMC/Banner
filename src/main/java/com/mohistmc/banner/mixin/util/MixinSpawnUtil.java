package com.mohistmc.banner.mixin.util;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnUtil.class)
public class MixinSpawnUtil {

    @Inject(method = "trySpawnMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V", shift = At.Shift.BEFORE))
    private static <T extends Mob> void banner$pushSpawnReason(EntityType<T> entityType, MobSpawnType spawnType, ServerLevel level, BlockPos pos, int attempts, int i, int j, SpawnUtil.Strategy strategy, CallbackInfoReturnable<Optional<T>> cir) {
        level.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.DEFAULT);
    }
}
