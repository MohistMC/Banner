package com.mohistmc.banner.injection.server.level;

import com.mohistmc.banner.injection.world.level.InjectionLevel;
import java.util.UUID;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.weather.LightningStrikeEvent;

public interface InjectionServerLevel extends InjectionLevel {

    default boolean addEntitySerialized(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        return false;
    }

    default  <T extends ParticleOptions> int sendParticles(T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed, boolean force) {
        return particleCount;
    }

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
