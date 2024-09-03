package com.mohistmc.banner.injection.server.level;

import com.mohistmc.banner.injection.world.level.InjectionLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.weather.LightningStrikeEvent;

import java.util.UUID;

public interface InjectionServerLevel extends InjectionLevel {

    default boolean addEntitySerialized(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        throw new IllegalStateException("Not implemented");
    }

    default  <T extends ParticleOptions> int sendParticles(T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed, boolean force) {
        throw new IllegalStateException("Not implemented");
    }

    default LevelStorageSource.LevelStorageAccess bridge$convertable() {
        throw new IllegalStateException("Not implemented");
    }

    default UUID bridge$uuid() {
        throw new IllegalStateException("Not implemented");
    }

    default LevelChunk getChunkIfLoaded(int x, int z) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean addWithUUID(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        throw new IllegalStateException("Not implemented");
    }

    default void addDuringTeleport(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean tryAddFreshEntityWithPassengers(Entity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean strikeLightning(Entity entitylightning) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean strikeLightning(Entity entitylightning, LightningStrikeEvent.Cause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default  <T extends ParticleOptions> int sendParticles(ServerPlayer sender, T t0, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6, boolean force) {
        throw new IllegalStateException("Not implemented");
    }

    default PrimaryLevelData bridge$serverLevelDataCB() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean canAddFreshEntity() {
        throw new IllegalStateException("Not implemented");
    }
}
