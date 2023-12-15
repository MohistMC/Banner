package com.mohistmc.banner.injection.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.PositionImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface InjectionEntity {

    default void banner$setBukkitEntity(CraftEntity bukkitEntity) {

    }

    default void setOrigin(@javax.annotation.Nonnull Location location) {

    }

    @Nullable
    default org.bukkit.util.Vector getOriginVector() {
        return null;
    }

    @Nullable
    default UUID getOriginWorld() {
        return null;
    }

    default boolean bridge$persist() {
        return true;
    }

    default void banner$setPersist(boolean persist) {
    }

    default boolean bridge$visibleByDefault() {
        return false;
    }

    default void banner$setVisibleByDefault(boolean visibleByDefault) {
    }

    default boolean bridge$valid() {
        return false;
    }

    default void banner$setValid(boolean valid) {
    }

    default int bridge$maxAirTicks() {
        return 0;
    }

    default void banner$setMaxAirTicks(int maxAirTicks) {
    }

    default org.bukkit.projectiles.ProjectileSource bridge$projectileSource() {
        return null;
    }

    default void banner$setProjectileSource(org.bukkit.projectiles.ProjectileSource projectileSource) {
    }

    default boolean bridge$lastDamageCancelled() {
        return false;
    }

    default void banner$setLastDamageCancelled(boolean lastDamageCancelled) {
    }

    default boolean bridge$persistentInvisibility() {
        return false;
    }

    default void banner$setPersistentInvisibility(boolean persistentInvisibility) {
    }

    default BlockPos bridge$lastLavaContact() {
        return null;
    }

    default void banner$setLastLavaContact(BlockPos lastLavaContact) {
    }

    default  boolean teleportTo(ServerLevel worldserver, double d0, double d1, double d2, Set<RelativeMovement> set, float f, float f1, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
        return false;
    }

    default CraftEntity getBukkitEntity() {
        return null;
    }

    default int getDefaultMaxAirSupply() {
        return 0;
    }

    default float getBukkitYaw() {
        return 0;
    }

    default boolean isChunkLoaded() {
        return false;
    }

    default void postTick() {
    }

    default void setSecondsOnFire(int i, boolean callEvent) {
    }

    default void banner$setSecondsOnFire(int i, boolean callEvent) {

    }

    default SoundEvent getSwimSound0() {
        return null;
    }

    default SoundEvent getSwimSplashSound0() {
        return null;
    }

    default SoundEvent getSwimHighSpeedSplashSound0() {
        return null;
    }

    default boolean canCollideWithBukkit(Entity entity) {
        return false;
    }

    default org.spigotmc.ActivationRange.ActivationType bridge$activationType() {
        return null;
    }

    default void inactiveTick() {

    }

    default Entity teleportTo(ServerLevel worldserver, PositionImpl location) {
        return null;
    }

    default long bridge$activatedTick() {
        return 0;
    }

    default void banner$setActivatedTick(long activatedTick) {

    }

    default boolean bridge$defaultActivationState() {
        return false;
    }

    default void banner$setDefaultActivationState(boolean state) {

    }

    default boolean bridge$generation() {
        return false;
    }

    default void banner$setGeneration(boolean gen) {
    }

    default boolean banner$removePassenger(Entity entity) {
        return false;
    }

    default CraftPortalEvent callPortalEvent(Entity entity, ServerLevel exitWorldServer, PositionImpl exitPosition, PlayerTeleportEvent.TeleportCause cause, int searchRadius, int creationRadius) {
        return null;
    }
}
