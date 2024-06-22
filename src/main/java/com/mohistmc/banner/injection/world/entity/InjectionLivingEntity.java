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
    }

    default Optional<EntityPotionEffectEvent.Cause> getEffectCause() {
        return Optional.empty();
    }

    default void pushHealReason(EntityRegainHealthEvent.RegainReason reason) {

    }

    default void pushEffectCause(EntityPotionEffectEvent.Cause cause) {

    }

    default int bridge$expToDrop() {
        return 0;
    }

    default void banner$setExpToDrop(int expToDrop) {
    }

    default boolean bridge$forceDrops() {
        return false;
    }

    default void banner$setForceDrops(boolean forceDrops) {
    }

    default ArrayList<org.bukkit.inventory.ItemStack> bridge$drops() {
        return null;
    }

    default void banner$setDrops(ArrayList<org.bukkit.inventory.ItemStack> drops) {
    }

    default CraftAttributeMap bridge$craftAttributes() {
        return null;
    }

    default void banner$setCraftAttributes(CraftAttributeMap craftAttributes) {
    }

    default boolean bridge$collides() {
        return false;
    }

    default void banner$setCollides(boolean collides) {
    }

    default Set<UUID> bridge$collidableExemptions() {
        return null;
    }

    default void banner$setCollidableExemptions(Set<UUID> collidableExemptions) {
    }

    default boolean bridge$bukkitPickUpLoot() {
        return false;
    }

    default void banner$setBukkitPickUpLoot(boolean bukkitPickUpLoot) {
    }

    default boolean bridge$isTickingEffects() {
        return false;
    }

    default void banner$setIsTickingEffects(boolean isTickingEffects) {
    }

    default List<ProcessableEffect> bridge$effectsToProcess() {
        return null;
    }

    default void banner$setEffectsToProcess(List<ProcessableEffect> effectsToProcess) {
    }

    default void onEquipItem(EquipmentSlot enumitemslot, ItemStack itemstack, ItemStack itemstack1, boolean silent) {
    }

    default boolean removeAllEffects(EntityPotionEffectEvent.Cause cause) {
        return false;
    }

    default boolean addEffect(MobEffectInstance mobeffect, EntityPotionEffectEvent.Cause cause) {
        return false;
    }

    default boolean addEffect(MobEffectInstance mobeffect, @Nullable Entity entity, EntityPotionEffectEvent.Cause cause) {
        return false;
    }

    @Nullable
    default MobEffectInstance c(@Nullable MobEffect mobeffectlist, EntityPotionEffectEvent.Cause cause) {
        return null;
    }

    default boolean removeEffect(Holder<MobEffect> holder, EntityPotionEffectEvent.Cause cause) {
        return false;
    }

    default void heal(float f, EntityRegainHealthEvent.RegainReason regainReason) {
    }

    default int getExpReward(@Nullable Entity entity) {
        return 0;
    }

    default SoundEvent getHurtSound0(DamageSource damagesource) {
        return null;
    }

    default SoundEvent getDeathSound0() {
        return null;
    }

    default SoundEvent getFallDamageSound0(int fallHeight) {
        return null;
    }

    default SoundEvent getDrinkingSound0(ItemStack itemstack) {
        return null;
    }

    default SoundEvent getEatingSound0(ItemStack itemstack) {
        return null;
    }

    default boolean damageEntity0(final DamageSource damagesource, float f) { // void -> boolean, add final
        return false;
    }

    default void setArrowCount(int i, boolean flag) {
    }

    default void setItemSlot(EquipmentSlot enumitemslot, ItemStack itemstack, boolean silent) {
    }
}
