package com.mohistmc.banner.mixin.world.level.spawner;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BaseSpawner.class)
public abstract class MixinBaseSpawner {

    @Shadow public SimpleWeightedRandomList<SpawnData> spawnPotentials;


    @Inject(method = "setEntityId", at = @At("RETURN"))
    public void banner$clearMobs(CallbackInfo ci) {
        this.spawnPotentials = SimpleWeightedRandomList.empty();
    }

    @Inject(method = "serverTick",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;tryAddFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$pushReason(ServerLevel serverLevel, BlockPos pos, CallbackInfo ci, boolean bl,
                                   RandomSource randomSource, SpawnData spawnData, int i,
                                   CompoundTag compoundTag, Optional optional, ListTag listTag,
                                   int j, double d, double e, double f, BlockPos blockPos,
                                   Entity entity) {
        // Spigot Start
        if (CraftEventFactory.callSpawnerSpawnEvent(entity, blockPos).isCancelled()) {
            Entity vehicle = entity.getVehicle();
            if (vehicle != null) {
                vehicle.discard();
            }
            for (Entity passenger : entity.getIndirectPassengers()) {
                passenger.discard();
            }
            return;
        }
        // Spigot End
        serverLevel.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.SPAWNER);
    }

    @Redirect(method = "isNearPlayer",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;hasNearbyAlivePlayer(DDDD)Z"))
    private boolean banner$affectSpawn(Level instance, double x, double y, double z, double v) {
        return instance.hasNearbyAlivePlayerThatAffectsSpawning(x, y, z, v); // Paper - Affects Spawning API
    }
}
