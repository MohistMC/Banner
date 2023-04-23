package com.mohistmc.banner.injection.server.level;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.WeatherType;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.event.player.PlayerSpawnChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public interface InjectionServerPlayer {

    default BlockPos getSpawnPoint(ServerLevel worldserver) {
        return null;
    }

    default String bridge$displayName() {
        return null;
    }

    default void spawnIn(Level world) {
    }

    default Entity changeDimension(ServerLevel worldserver, PlayerTeleportEvent.TeleportCause cause) {
        return null;
    }

    default Optional<BlockUtil.FoundRectangle> getExitPortal(ServerLevel worldserver, BlockPos blockposition, boolean flag, WorldBorder worldborder, int searchRadius, boolean canCreatePortal, int createRadius) { // CraftBukkit
        return null;
    }

    default Either<Player.BedSleepingProblem, Unit> getBedResult(BlockPos blockposition, Direction enumdirection) {
        return null;
    }

    default boolean teleportTo(ServerLevel worldserver, double d0, double d1, double d2, Set<RelativeMovement> set, float f, float f1, PlayerTeleportEvent.TeleportCause cause) {
        return false;
    }

    default void teleportTo(ServerLevel worldserver, double d0, double d1, double d2, float f, float f1, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
    }

    default void setRespawnPosition(ResourceKey<Level> resourcekey, @Nullable BlockPos blockposition, float f, boolean flag, boolean flag1, PlayerSpawnChangeEvent.Cause cause) {
    }

    default long getPlayerTime() {
        return 0;
    }

    default WeatherType getPlayerWeather() {
        return null;
    }

    default void setPlayerWeather(WeatherType type, boolean plugin) {
    }

    default void updateWeather(float oldRain, float newRain, float oldThunder, float newThunder) {
    }

    default void tickWeather() {
    }

    default void resetPlayerWeather() {
    }

    default void forceSetPositionRotation(double x, double y, double z, float yaw, float pitch) {
    }

    default void reset() {
    }

    default CraftPlayer getBukkitEntity() {
        return null;
    }
}
