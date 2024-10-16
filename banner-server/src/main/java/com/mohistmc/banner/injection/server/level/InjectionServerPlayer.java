package com.mohistmc.banner.injection.server.level;

import com.mohistmc.banner.injection.world.entity.player.InjectionPlayer;
import com.mojang.datafixers.util.Either;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.WeatherType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.player.PlayerSpawnChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public interface InjectionServerPlayer extends InjectionPlayer {

    @Override
    default CraftPlayer getBukkitEntity() {
        throw new IllegalStateException("Not implemented");
    }

    default int nextContainerCounterInt() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean banner$initialized() {
        throw new IllegalStateException("Not implemented");
    }

    default String bridge$locale() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setLocale(String locale) {
        throw new IllegalStateException("Not implemented");
    }

    default long bridge$timeOffset() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setTimeOffset(long timeOffset) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$relativeTime() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setRelativeTime(boolean relativeTime) {
        throw new IllegalStateException("Not implemented");
    }

    default Component bridge$listName() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setListName(Component listName) {
        throw new IllegalStateException("Not implemented");
    }

    default org.bukkit.Location bridge$compassTarget() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setCompassTarget(org.bukkit.Location compassTarget) {
        throw new IllegalStateException("Not implemented");
    }

    default int bridge$newExp(){
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setNewExp(int newExp) {
        throw new IllegalStateException("Not implemented");
    }

    default int bridge$newLevel() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setNewLevel(int newLevel) {
        throw new IllegalStateException("Not implemented");
    }

    default int bridge$newTotalExp() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setNewTotalExp(int newTotalExp) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$keepLevel() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setKeepLevel(boolean keepLevel) {
        throw new IllegalStateException("Not implemented");
    }

    default double bridge$maxHealthCache() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setMaxHealthCache(double maxHealthCache) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$joining() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setJoining(boolean joining) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$sentListPacket() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setSentListPacket(boolean sentListPacket) {
        throw new IllegalStateException("Not implemented");
    }

    default Integer bridge$clientViewDistance() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setClientViewDistance(Integer clientViewDistance) {
        throw new IllegalStateException("Not implemented");
    }

    default String bridge$kickLeaveMessage() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setKickLeaveMessage(String kickLeaveMessage) {
        throw new IllegalStateException("Not implemented");
    }

    default BlockPos getSpawnPoint(ServerLevel worldserver) {
        throw new IllegalStateException("Not implemented");
    }

    default String bridge$displayName() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setDisplayName(String displayName) {
        throw new IllegalStateException("Not implemented");
    }

    default void spawnIn(Level world) {
        throw new IllegalStateException("Not implemented");
    }

    default Entity changeDimension(ServerLevel worldserver, PlayerTeleportEvent.TeleportCause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default Optional<BlockUtil.FoundRectangle> getExitPortal(ServerLevel worldserver, BlockPos blockposition, boolean flag, WorldBorder worldborder, int searchRadius, boolean canCreatePortal, int createRadius) { // CraftBukkit
        throw new IllegalStateException("Not implemented");
    }

    default Either<Player.BedSleepingProblem, Unit> getBedResult(BlockPos blockposition, Direction enumdirection) {
        throw new IllegalStateException("Not implemented");
    }

    default void teleportTo(ServerLevel worldserver, double d0, double d1, double d2, float f, float f1, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default void setRespawnPosition(ResourceKey<Level> resourcekey, @Nullable BlockPos blockposition, float f, boolean flag, boolean flag1, PlayerSpawnChangeEvent.Cause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default long getPlayerTime() {
        throw new IllegalStateException("Not implemented");
    }

    default WeatherType getPlayerWeather() {
        throw new IllegalStateException("Not implemented");
    }

    default void setPlayerWeather(WeatherType type, boolean plugin) {
        throw new IllegalStateException("Not implemented");
    }

    default void updateWeather(float oldRain, float newRain, float oldThunder, float newThunder) {
        throw new IllegalStateException("Not implemented");
    }

    default void tickWeather() {
        throw new IllegalStateException("Not implemented");
    }

    default void resetPlayerWeather() {
        throw new IllegalStateException("Not implemented");
    }

    default void forceSetPositionRotation(double x, double y, double z, float yaw, float pitch) {
        throw new IllegalStateException("Not implemented");
    }

    default void reset() {
        throw new IllegalStateException("Not implemented");
    }

    default void pushChangeDimensionCause(PlayerTeleportEvent.TeleportCause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default void pushChangeSpawnCause(PlayerSpawnChangeEvent.Cause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default CraftPlayer.TransferCookieConnection bridge$transferCookieConnection() {
        throw new IllegalStateException("Not implemented");
    }
}
