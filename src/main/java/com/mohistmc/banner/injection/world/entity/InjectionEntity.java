package com.mohistmc.banner.injection.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;

public interface InjectionEntity {

    default boolean bridge$persist() {
        return false;
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

    default Entity teleportTo(ServerLevel worldserver, Position location) {
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
}
