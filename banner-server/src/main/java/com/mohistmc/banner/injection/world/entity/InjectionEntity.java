package com.mohistmc.banner.injection.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.event.CraftPortalEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface InjectionEntity {

    default boolean bridge$inWorld() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setInWorld(boolean inWorld) {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setBukkitEntity(CraftEntity bukkitEntity) {
        throw new IllegalStateException("Not implemented");
    }

    default void setOrigin(@javax.annotation.Nonnull Location location) {
        throw new IllegalStateException("Not implemented");
    }

    default void refreshEntityData(ServerPlayer to) {
        throw new IllegalStateException("Not implemented");
    }

    @Nullable
    default org.bukkit.util.Vector getOriginVector() {
        throw new IllegalStateException("Not implemented");
    }

    @Nullable
    default UUID getOriginWorld() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$persist() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setPersist(boolean persist) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$visibleByDefault() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setVisibleByDefault(boolean visibleByDefault) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$valid() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setValid(boolean valid) {
        throw new IllegalStateException("Not implemented");
    }

    default int bridge$maxAirTicks() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setMaxAirTicks(int maxAirTicks) {
        throw new IllegalStateException("Not implemented");
    }

    default org.bukkit.projectiles.ProjectileSource bridge$projectileSource() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setProjectileSource(org.bukkit.projectiles.ProjectileSource projectileSource) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$lastDamageCancelled() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setLastDamageCancelled(boolean lastDamageCancelled) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$persistentInvisibility() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setPersistentInvisibility(boolean persistentInvisibility) {
        throw new IllegalStateException("Not implemented");
    }

    default BlockPos bridge$lastLavaContact() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setLastLavaContact(BlockPos lastLavaContact) {
        throw new IllegalStateException("Not implemented");
    }

    default  boolean teleportTo(ServerLevel worldserver, double d0, double d1, double d2, Set<RelativeMovement> set, float f, float f1, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default CraftEntity getBukkitEntity() {
        throw new IllegalStateException("Not implemented");
    }

    default int getDefaultMaxAirSupply() {
        throw new IllegalStateException("Not implemented");
    }

    default float getBukkitYaw() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean isChunkLoaded() {
        throw new IllegalStateException("Not implemented");
    }

    default void postTick() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setSecondsOnFire(float i, boolean callEvent) {
        throw new IllegalStateException("Not implemented");
    }

    default SoundEvent getSwimSound0() {
        throw new IllegalStateException("Not implemented");
    }

    default SoundEvent getSwimSplashSound0() {
        throw new IllegalStateException("Not implemented");
    }

    default SoundEvent getSwimHighSpeedSplashSound0() {
        throw new IllegalStateException("Not implemented");
    }

    default boolean canCollideWithBukkit(Entity entity) {
        throw new IllegalStateException("Not implemented");
    }

    default org.spigotmc.ActivationRange.ActivationType bridge$activationType() {
        throw new IllegalStateException("Not implemented");
    }

    default Entity teleportTo(ServerLevel worldserver, Vec3 location) {
        throw new IllegalStateException("Not implemented");
    }

    default long bridge$activatedTick() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setActivatedTick(long activatedTick) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$defaultActivationState() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setDefaultActivationState(boolean state) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$generation() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setGeneration(boolean gen) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean banner$removePassenger(Entity entity) {
        throw new IllegalStateException("Not implemented");
    }

    default CraftPortalEvent callPortalEvent(Entity entity, ServerLevel exitWorldServer, Vec3 exitPosition, PlayerTeleportEvent.TeleportCause cause, int searchRadius, int creationRadius) {
        throw new IllegalStateException("Not implemented");
    }

    default void discard(EntityRemoveEvent.Cause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default void remove(Entity.RemovalReason entity_removalreason, EntityRemoveEvent.Cause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default void setRemoved(Entity.RemovalReason entity_removalreason, EntityRemoveEvent.Cause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default void pushRemoveCause(EntityRemoveEvent.Cause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default void igniteForSeconds(float i, boolean callEvent) {
        throw new IllegalStateException("Not implemented");
    }
}
