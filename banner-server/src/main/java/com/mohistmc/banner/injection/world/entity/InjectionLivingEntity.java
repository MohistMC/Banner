package com.mohistmc.banner.injection.world.entity;

import com.mohistmc.banner.bukkit.ProcessableEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.attribute.CraftAttributeMap;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.jetbrains.annotations.Nullable;

public interface InjectionLivingEntity extends InjectionEntity {

    default void equipEventAndSound(EquipmentSlot slot, ItemStack oldItem, ItemStack newItem, boolean silent) {
        throw new IllegalStateException("Not implemented");
    }

    default Optional<EntityPotionEffectEvent.Cause> getEffectCause() {
        return Optional.empty();
    }

    default void pushHealReason(EntityRegainHealthEvent.RegainReason reason) {
        throw new IllegalStateException("Not implemented");
    }

    default void pushEffectCause(EntityPotionEffectEvent.Cause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default int bridge$expToDrop() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setExpToDrop(int expToDrop) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$forceDrops() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setForceDrops(boolean forceDrops) {
        throw new IllegalStateException("Not implemented");
    }

    default ArrayList<org.bukkit.inventory.ItemStack> bridge$drops() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setDrops(ArrayList<org.bukkit.inventory.ItemStack> drops) {
        throw new IllegalStateException("Not implemented");
    }

    default CraftAttributeMap bridge$craftAttributes() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setCraftAttributes(CraftAttributeMap craftAttributes) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$collides() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setCollides(boolean collides) {
        throw new IllegalStateException("Not implemented");
    }

    default Set<UUID> bridge$collidableExemptions() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setCollidableExemptions(Set<UUID> collidableExemptions) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$bukkitPickUpLoot() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setBukkitPickUpLoot(boolean bukkitPickUpLoot) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$isTickingEffects() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setIsTickingEffects(boolean isTickingEffects) {
        throw new IllegalStateException("Not implemented");
    }

    default List<ProcessableEffect> bridge$effectsToProcess() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setEffectsToProcess(List<ProcessableEffect> effectsToProcess) {
        throw new IllegalStateException("Not implemented");
    }

    default void onEquipItem(EquipmentSlot enumitemslot, ItemStack itemstack, ItemStack itemstack1, boolean silent) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean removeAllEffects(EntityPotionEffectEvent.Cause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean addEffect(MobEffectInstance mobeffect, EntityPotionEffectEvent.Cause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean addEffect(MobEffectInstance mobeffect, @Nullable Entity entity, EntityPotionEffectEvent.Cause cause) {
        throw new IllegalStateException("Not implemented");
    }

    @Nullable
    default MobEffectInstance c(@Nullable MobEffect mobeffectlist, EntityPotionEffectEvent.Cause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean removeEffect(Holder<MobEffect> holder, EntityPotionEffectEvent.Cause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default void heal(float f, EntityRegainHealthEvent.RegainReason regainReason) {
        throw new IllegalStateException("Not implemented");
    }

    default int getExpReward(@Nullable Entity entity) {
        throw new IllegalStateException("Not implemented");
    }

    default SoundEvent getHurtSound0(DamageSource damagesource) {
        throw new IllegalStateException("Not implemented");
    }

    default SoundEvent getDeathSound0() {
        throw new IllegalStateException("Not implemented");
    }

    default SoundEvent getFallDamageSound0(int fallHeight) {
        throw new IllegalStateException("Not implemented");
    }

    default SoundEvent getDrinkingSound0(ItemStack itemstack) {
        throw new IllegalStateException("Not implemented");
    }

    default SoundEvent getEatingSound0(ItemStack itemstack) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean damageEntity0(final DamageSource damagesource, float f) { // void -> boolean, add final
        throw new IllegalStateException("Not implemented");
    }

    default void setArrowCount(int i, boolean flag) {
        throw new IllegalStateException("Not implemented");
    }

    default void setItemSlot(EquipmentSlot enumitemslot, ItemStack itemstack, boolean silent) {
        throw new IllegalStateException("Not implemented");
    }
}
