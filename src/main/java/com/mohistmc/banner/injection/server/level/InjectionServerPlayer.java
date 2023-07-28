package com.mohistmc.banner.injection.server.level;

import com.mohistmc.banner.injection.world.entity.player.InjectionPlayer;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalInfo;
import org.bukkit.WeatherType;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.event.player.PlayerSpawnChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public interface InjectionServerPlayer extends InjectionPlayer {

    @Override
    default CraftPlayer getBukkitEntity() {
        return null;
    }

    default int nextContainerCounter() {
        return 0;
    }

    default int nextContainerCounterInt() {
        return 0;
    }

    default boolean banner$initialized() {
        return false;
    }

    default String bridge$locale() {
        return null;
    }

    default void banner$setLocale(String locale) {
    }

    default long bridge$timeOffset() {
        return 0;
    }

    default void banner$setTimeOffset(long timeOffset) {
    }

    default boolean bridge$relativeTime() {
        return false;
    }

    default void banner$setRelativeTime(boolean relativeTime) {
    }

    default Component bridge$listName() {
        return null;
    }

    default void banner$setListName(Component listName) {
    }

    default org.bukkit.Location bridge$compassTarget() {
        return null;
    }

    default void banner$setCompassTarget(org.bukkit.Location compassTarget) {
    }

    default int bridge$newExp(){
        return 0;
    }

    default void banner$setNewExp(int newExp) {
    }

    default int bridge$newLevel() {
        return 0;
    }

    default void banner$setNewLevel(int newLevel) {
    }

    default int bridge$newTotalExp() {
        return 0;
    }

    default void banner$setNewTotalExp(int newTotalExp) {
    }

    default boolean bridge$keepLevel() {
        return false;
    }

    default void banner$setKeepLevel(boolean keepLevel) {
    }

    default double bridge$maxHealthCache() {
        return 0;
    }

    default void banner$setMaxHealthCache(double maxHealthCache) {
    }

    default boolean bridge$joining() {
        return false;
    }

    default void banner$setJoining(boolean joining) {
    }

    default boolean bridge$sentListPacket() {
        return false;
    }

    default void banner$setSentListPacket(boolean sentListPacket) {
    }

    default Integer bridge$clientViewDistance() {
        return 0;
    }

    default void banner$setClientViewDistance(Integer clientViewDistance) {
    }

    default String bridge$kickLeaveMessage() {
        return null;
    }

    default void banner$setKickLeaveMessage(String kickLeaveMessage) {
    }

    default BlockPos getSpawnPoint(ServerLevel worldserver) {
        return null;
    }

    default String bridge$displayName() {
        return null;
    }

    default void banner$setDisplayName(String displayName) {
    }

    default void spawnIn(Level world) {
    }

    default Entity changeDimension(ServerLevel worldserver, PlayerTeleportEvent.TeleportCause cause) {
        return null;
    }

    default Optional<BlockUtil.FoundRectangle> getExitPortal(ServerLevel worldserver, BlockPos blockposition, boolean flag, WorldBorder worldborder, int searchRadius, boolean canCreatePortal, int createRadius) { // CraftBukkit
        return Optional.empty();
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

    default void pushChangeDimensionCause(PlayerTeleportEvent.TeleportCause cause) {

    }

    default Optional<PlayerTeleportEvent.TeleportCause> bridge$teleportCause() {
        return Optional.empty();
    }

    default void pushChangeSpawnCause(PlayerSpawnChangeEvent.Cause cause) {

    }

    default PortalInfo banner$findDimensionEntryPoint(ServerLevel destination) {
        return null;
    }

    default PlayerTeleportEvent.TeleportCause bridge$changeDimensionCause() {
        return null;
    }
}
