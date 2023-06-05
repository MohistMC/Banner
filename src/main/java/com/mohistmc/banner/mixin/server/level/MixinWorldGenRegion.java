package com.mohistmc.banner.mixin.server.level;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.WorldGenLevel;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public abstract class MixinWorldGenRegion implements WorldGenLevel {

    @Inject(method = "addFreshEntity", at = @At("HEAD"))
    private void banner$addReason(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        entity.level().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.DEFAULT);
    }

    @Override
    public boolean addFreshEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        return this.addFreshEntity(entity);
    }

    @Override
    public CreatureSpawnEvent.SpawnReason getAddEntityReason() {
        return CreatureSpawnEvent.SpawnReason.DEFAULT;
    }

}
