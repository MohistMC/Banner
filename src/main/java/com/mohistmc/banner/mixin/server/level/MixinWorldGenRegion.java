package com.mohistmc.banner.mixin.server.level;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.WorldGenLevel;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WorldGenRegion.class)
public abstract class MixinWorldGenRegion implements WorldGenLevel {

    @Override
    public void addFreshEntityWithPassengers(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
    }

}
