package com.mohistmc.banner.injection.server.level;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.weather.LightningStrikeEvent;

import java.util.UUID;

public interface InjectionServerLevel {

    default LevelStorageSource.LevelStorageAccess bridge$convertable() {
        return null;
    }

    default UUID bridge$uuid() {
        return null;
    }

    default LevelChunk getChunkIfLoaded(int x, int z) {
        return null;
    }

    default boolean addWithUUID(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        return false;
    }

    default void addDuringTeleport(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
    }

    default boolean addEntity(Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {
        return false;
    }

    default boolean tryAddFreshEntityWithPassengers(Entity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        return false;
    }

    default boolean strikeLightning(Entity entitylightning) {
        return false;
    }

    default boolean strikeLightning(Entity entitylightning, LightningStrikeEvent.Cause cause) {
        return false;
    }

    default  <T extends ParticleOptions> int sendParticles(ServerPlayer sender, T t0, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6, boolean force) {
        return i;
    }

    default PrimaryLevelData bridge$serverLevelDataCB() {
        return null;
    }
}
