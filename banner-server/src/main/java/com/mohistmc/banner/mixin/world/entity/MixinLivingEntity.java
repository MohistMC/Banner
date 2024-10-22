package com.mohistmc.banner.mixin.world.entity;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import com.mohistmc.banner.bukkit.EntityDamageResult;
import com.mohistmc.banner.bukkit.ProcessableEffect;
import com.mohistmc.banner.injection.world.entity.InjectionLivingEntity;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Eject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements Attackable, InjectionLivingEntity {

    @Shadow @Final public static EntityDataAccessor<Float> DATA_HEALTH_ID;
    @Shadow @Final private AttributeMap attributes;

    @Shadow public abstract SoundEvent getEatingSound(net.minecraft.world.item.ItemStack stack);

    @Shadow protected abstract SoundEvent getDrinkingSound(net.minecraft.world.item.ItemStack stack);

    @Shadow protected abstract SoundEvent getFallDamageSound(int height);

    @Shadow @Nullable protected abstract SoundEvent getDeathSound();

    @Shadow public abstract void onEquipItem(EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2);
    @Shadow @Final public Map<Holder<MobEffect>, MobEffectInstance> activeEffects;

    @Shadow protected abstract void onEffectUpdated(MobEffectInstance effectInstance, boolean forced, @Nullable Entity entity);

    @Shadow protected abstract void onEffectRemoved(MobEffectInstance effectInstance);

    @Shadow public boolean effectsDirty;

    @Shadow protected abstract void updateInvisibilityStatus();
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_EFFECT_AMBIENCE_ID;

    @Shadow public abstract boolean canBeAffected(MobEffectInstance effectInstance);

    @Shadow protected abstract void onEffectAdded(MobEffectInstance instance, @Nullable Entity entity);
    @Shadow public abstract boolean wasExperienceConsumed();

    @Shadow protected abstract boolean isAlwaysExperienceDropper();

    @Shadow protected int lastHurtByPlayerTime;

    @Shadow public abstract boolean shouldDropExperience();

    @Shadow public abstract boolean removeAllEffects();

    @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot slot);

    @Shadow public abstract boolean isDamageSourceBlocked(DamageSource damageSource);

    @Shadow protected abstract float getDamageAfterArmorAbsorb(DamageSource damageSource, float damageAmount);

    @Shadow protected abstract float getDamageAfterMagicAbsorb(DamageSource damageSource, float damageAmount);

    @Shadow public abstract float getAbsorptionAmount();

    @Shadow public abstract void hurtHelmet(DamageSource damageSource, float damageAmount);

    @Shadow public abstract void hurtArmor(DamageSource damageSource, float damageAmount);

    @Shadow public abstract void hurtCurrentlyUsedShield(float damageAmount);

    @Shadow protected abstract void blockUsingShield(LivingEntity attacker);

    @Shadow public abstract void setAbsorptionAmount(float absorptionAmount);

    @Shadow public abstract float getHealth();

    @Shadow public abstract CombatTracker getCombatTracker();

    @Shadow public abstract void setHealth(float health);

    @Shadow public abstract void heal(float healAmount);
    @Shadow public abstract ItemStack getItemInHand(InteractionHand hand);

    @Shadow public abstract boolean onClimbable();

    @Shadow public abstract InteractionHand getUsedItemHand();

    @Shadow @Final public static EntityDataAccessor<Integer> DATA_ARROW_COUNT_ID;

    @Shadow public abstract int getArrowCount();

    @Shadow protected abstract boolean doesEmitEquipEvent(EquipmentSlot slot);

    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract void indicateDamage(double d, double e);

    @Shadow public abstract ItemStack eat(Level level, ItemStack food);

    @Shadow protected boolean dead;

    @Shadow public abstract boolean isSleeping();

    @Shadow public abstract void stopSleeping();

    @Shadow @Final public WalkAnimationState walkAnimation;

    @Shadow public int invulnerableDuration;

    @Shadow public float lastHurt;

    @Shadow protected abstract void actuallyHurt(DamageSource damageSource, float f);

    @Shadow public abstract void die(DamageSource damageSource);

    @Shadow protected abstract void playHurtSound(DamageSource damageSource);

    @Shadow public abstract boolean isDeadOrDying();

    @Shadow protected abstract float getSoundVolume();

    @Shadow public abstract float getVoicePitch();

    @Shadow @Nullable private DamageSource lastDamageSource;

    @Shadow private long lastDamageStamp;

    @Shadow public abstract void knockback(double d, double e, double f);

    @Shadow @Nullable public Player lastHurtByPlayer;

    @Shadow public abstract void setLastHurtByMob(@Nullable LivingEntity livingEntity);

    @Shadow public int hurtDuration;

    @Shadow public int hurtTime;

    @Shadow protected int noActionTime;

    @Shadow public abstract double getAttributeValue(Holder<Attribute> holder);

    @Shadow @Nullable public abstract AttributeInstance getAttribute(Holder<Attribute> holder);

    @Shadow public abstract boolean hasEffect(Holder<MobEffect> holder);

    @Shadow public abstract int getExperienceReward(ServerLevel serverLevel, @Nullable Entity entity);

    @Shadow public abstract boolean addEffect(MobEffectInstance mobEffectInstance, @Nullable Entity entity);

    @Shadow @Nullable public abstract MobEffectInstance getEffect(Holder<MobEffect> holder);

    @Shadow public abstract int getArmorValue();

    public MixinLivingEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public int expToDrop;
    public boolean forceDrops;
    public ArrayList<org.bukkit.inventory.ItemStack> drops = new ArrayList<>();
    public CraftAttributeMap craftAttributes;
    public boolean collides = true;
    public Set<UUID> collidableExemptions = new HashSet<>();
    public boolean bukkitPickUpLoot;
    private boolean isTickingEffects = false;
    private List<ProcessableEffect> effectsToProcess = Lists.newArrayList();

    // Banner - add fields
    private AtomicReference<BlockState> banner$FallState = new AtomicReference<>();
    private AtomicBoolean banner$silent = new AtomicBoolean(false);
    private transient EntityPotionEffectEvent.Cause banner$cause;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"))
    private void banner$muteHealth(LivingEntity entity, float health) {
        // do nothing
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(EntityType<? extends LivingEntity> type, Level worldIn, CallbackInfo ci) {
        this.collides = true;
        this.craftAttributes = new CraftAttributeMap(this.attributes);
        this.entityData.set(DATA_HEALTH_ID, (float) this.getAttributeValue(Attributes.MAX_HEALTH));
    }

    @Inject(method = "checkFallDamage", at = @At("HEAD"))
    private void banner$getFallInfo(double y, boolean onGround, BlockState state, BlockPos pos, CallbackInfo ci) {
        this.banner$FallState.set(state);
    }

    @Redirect(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private <T extends ParticleOptions>  int banner$addCheckFall(ServerLevel instance, T particleOptions, double d, double e, double f, int i, double g, double h, double j, double k) {
        // CraftBukkit start - visiblity api
        float banner$f = (float) Mth.ceil(this.fallDistance - 3.0F);
        double banner$d = Math.min((double)(0.2F + banner$f / 15.0F), 2.5);
        int banner$i = (int)(150.0 * banner$d);
        if (((LivingEntity) (Object) this) instanceof ServerPlayer) {
            return ((ServerLevel) this.level()).sendParticles((ServerPlayer) (Object) this, new BlockParticleOption(ParticleTypes.BLOCK, banner$FallState.get()), this.getX(), this.getY(), this.getZ(), banner$i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, false);
        } else {
            return ((ServerLevel) this.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, banner$FallState.get()), this.getX(), this.getY(), this.getZ(), banner$i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D);
        }
    }


    @Redirect(method = "onEquipItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z"))
    private boolean banner$addSilentCheck(Level instance) {
        return !this.level().isClientSide() && !this.isSilent() && !banner$silent.getAndSet(false);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    public void banner$readMaxHealth(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("Bukkit.MaxHealth")) {
            Tag nbtbase = compound.get("Bukkit.MaxHealth");
            if (nbtbase.getId() == 5) {
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(((FloatTag) nbtbase).getAsDouble());
            } else if (nbtbase.getId() == 3) {
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(((IntTag) nbtbase).getAsDouble());
            }
        }
    }


    @Inject(method = "getHealth", cancellable = true, at = @At("HEAD"))
    public void banner$scaledHealth(CallbackInfoReturnable<Float> cir) {
        if (((LivingEntity) (Object) this) instanceof ServerPlayer && ((ServerPlayer) (Object) this).banner$initialized()) {
            cir.setReturnValue((float) ((ServerPlayer) (Object) this).getBukkitEntity().getHealth());
        }
    }

    @Inject(method = "setHealth", cancellable = true, at = @At("HEAD"))
    public void banner$setScaled(float health, CallbackInfo ci) {
        // CraftBukkit start - Handle scaled health
        if (((LivingEntity) (Object) this) instanceof ServerPlayer && ((ServerPlayer) (Object) this).banner$initialized()) {
            CraftPlayer player = ((ServerPlayer) (Object) this).getBukkitEntity();

            // Squeeze
            if (health < 0.0F) {
                player.setRealHealth(0.0D);
            } else if (health > player.getMaxHealth()) {
                player.setRealHealth(player.getMaxHealth());
            } else {
                player.setRealHealth(health);
            }
            player.updateScaledHealth(false);
            ci.cancel();
        }
        // CraftBukkit end
    }

    @Inject(method = "tickEffects", at = @At("HEAD"))
    private void banner$startTicking(CallbackInfo ci) {
        this.isTickingEffects = true;
    }

    @Decorate(method = "tickEffects", inject = true, at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V"))
    private void banner$effectExpire(@io.izzel.arclight.mixin.Local(ordinal = -1) MobEffectInstance mobeffect) throws Throwable {
        EntityPotionEffectEvent event = CraftEventFactory.callEntityPotionEffectChangeEvent((LivingEntity) (Object) this, mobeffect, null, EntityPotionEffectEvent.Cause.EXPIRATION);
        if (event.isCancelled()) {
            throw DecorationOps.jumpToLoopStart();
        }
    }

    @Inject(method = "tickEffects", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/world/entity/LivingEntity;effectsDirty:Z"))
    private void banner$pendingEffects(CallbackInfo ci) {
        isTickingEffects = false;
        for (ProcessableEffect e : effectsToProcess) {
            EntityPotionEffectEvent.Cause cause = e.getCause();
            pushEffectCause(cause);
            if (e.getEffect() != null) {
                addEffect(e.getEffect(), e.getCause());
            } else {
                removeEffect(e.getType(), e.getCause());
            }
        }
        effectsToProcess.clear();
    }

    @Decorate(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", inject = true, at = @At(value = "JUMP", opcode = Opcodes.IFNE, ordinal = 0))
    private void banner$addPendingEffects(MobEffectInstance mobEffectInstance, Entity entity,
                                          @io.izzel.arclight.mixin.Local(allocate = "cause") EntityPotionEffectEvent.Cause cause) throws Throwable {
        cause = getEffectCause().orElse(EntityPotionEffectEvent.Cause.UNKNOWN);
        if (isTickingEffects) {
            effectsToProcess.add(new ProcessableEffect(mobEffectInstance, cause));
            DecorationOps.cancel().invoke(true);
            return;
        }
        DecorationOps.blackhole().invoke();
    }

    @Decorate(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", inject = true,
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private void banner$effectAdd(MobEffectInstance mobEffectInstance, Entity entity, @io.izzel.arclight.mixin.Local(allocate = "cause") EntityPotionEffectEvent.Cause cause) throws Throwable {
        var event = CraftEventFactory.callEntityPotionEffectChangeEvent((LivingEntity) (Object) this, null, mobEffectInstance, cause, false);
        if (event.isCancelled()) {
            DecorationOps.cancel().invoke(false);
            return;
        }
        DecorationOps.blackhole().invoke();
    }

    @Decorate(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;update(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private boolean banner$effectReplace(MobEffectInstance oldEffect, MobEffectInstance newEffect,
                                         MobEffectInstance mobEffectInstance, Entity entity, @io.izzel.arclight.mixin.Local(allocate = "cause") EntityPotionEffectEvent.Cause cause) throws Throwable {
        var override = new MobEffectInstance(oldEffect).update(newEffect);
        var event = CraftEventFactory.callEntityPotionEffectChangeEvent((LivingEntity) (Object) this, oldEffect, newEffect, cause, override);
        if (event.isCancelled()) {
            return (boolean) DecorationOps.cancel().invoke(false);
        }
        if (event.isOverride()) {
            var b = (boolean) DecorationOps.callsite().invoke(oldEffect, newEffect);
            DecorationOps.blackhole().invoke(b);
        }
        return event.isOverride();
    }

    // Banner - fix mixin(locals = LocalCapture.CAPTURE_FAILHARD)
    public EntityPotionEffectEvent.Cause cause;

    @Override
    public boolean addEffect(MobEffectInstance effect, Entity entity, EntityPotionEffectEvent.Cause cause) {
        pushEffectCause(cause);
        return this.addEffect(effect, entity);
    }

    @Inject(method = "removeEffectNoUpdate", cancellable = true, at = @At("HEAD"))
    public void banner$clearActive(Holder<MobEffect> holder, CallbackInfoReturnable<MobEffectInstance> cir) {
        EntityPotionEffectEvent.Cause cause = getEffectCause().orElse(EntityPotionEffectEvent.Cause.UNKNOWN);
        if (isTickingEffects) {
            effectsToProcess.add(new ProcessableEffect(holder, cause));
            cir.setReturnValue(null);
            return;
        }

        MobEffectInstance effectInstance = this.activeEffects.get(holder);
        if (effectInstance == null) {
            cir.setReturnValue(null);
            return;
        }

        EntityPotionEffectEvent event = CraftEventFactory.callEntityPotionEffectChangeEvent((LivingEntity) (Object) this, effectInstance, null, cause);
        if (event.isCancelled()) {
            cir.setReturnValue(null);
        }
    }

    @Override
    public int getExpReward(@Nullable Entity entity) {
        if (this.level() instanceof ServerLevel serverLevel && !this.wasExperienceConsumed() && (this.isAlwaysExperienceDropper() || this.lastHurtByPlayerTime > 0 && this.shouldDropExperience() && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))) {
            int exp = this.getExperienceReward(serverLevel, entity);
            return exp;
        } else {
            return 0;
        }
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public boolean isAlive() {
        return !this.isRemoved() && this.entityData.get(DATA_HEALTH_ID) > 0.0F;
    }

    private transient boolean banner$damageResult;
    @Unique protected transient EntityDamageResult entityDamageResult;

    @Decorate(method = "hurt", inject = true, at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;noActionTime:I"))
    private void banner$entityDamageEvent(DamageSource damagesource, float originalDamage) throws Throwable {
        banner$damageResult = false;
        entityDamageResult = null;
        final boolean human = (Object) this instanceof net.minecraft.world.entity.player.Player;

        float damage = originalDamage;

        Function<Double, Double> blocking = f -> -((this.isDamageSourceBlocked(damagesource)) ? f : 0.0);
        float blockingModifier = blocking.apply((double) damage).floatValue();
        damage += blockingModifier;

        Function<Double, Double> freezing = f -> {
            if (damagesource.is(DamageTypeTags.IS_FREEZING) && this.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
                return -(f - (f * 5.0F));
            }
            return -0.0;
        };
        float freezingModifier = freezing.apply((double) damage).floatValue();
        damage += freezingModifier;

        Function<Double, Double> hardHat = f -> {
            if (damagesource.is(DamageTypeTags.DAMAGES_HELMET) && !this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                return -(f - (f * 0.75F));
            }
            return -0.0;
        };
        float hardHatModifier = hardHat.apply((double) damage).floatValue();
        damage += hardHatModifier;

        if ((float) this.invulnerableTime > (float) this.invulnerableDuration / 2.0F && !damagesource.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
            if (damage <= this.lastHurt) {
                if (damagesource.getEntity() instanceof net.minecraft.world.entity.player.Player) {
                    ((net.minecraft.world.entity.player.Player) damagesource.getEntity()).resetAttackStrengthTicker();
                }
                return;
            }
        }

        Function<Double, Double> armor = f -> {
            if (!damagesource.is(DamageTypeTags.BYPASSES_ARMOR)) {
                return -(f - CombatRules.getDamageAfterAbsorb((LivingEntity) (Object) this, f.floatValue(), damagesource, (float) this.getArmorValue(), (float) this.getAttributeValue(Attributes.ARMOR_TOUGHNESS)));
            }

            return -0.0;
        };
        float originalArmorDamage = damage;
        float armorModifier = armor.apply((double) damage).floatValue();
        damage += armorModifier;

        Function<Double, Double> resistance = f -> {
            if (!damagesource.is(DamageTypeTags.BYPASSES_EFFECTS) && this.hasEffect(MobEffects.DAMAGE_RESISTANCE) && !damagesource.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
                int i = (this.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f1 = f.floatValue() * (float) j;
                return -(f - (f1 / 25.0F));
            }
            return -0.0;
        };
        float resistanceModifier = resistance.apply((double) damage).floatValue();
        damage += resistanceModifier;

        Function<Double, Double> magic = f -> {
            float l;
            if (this.level() instanceof ServerLevel serverLevel) {
                l = EnchantmentHelper.getDamageProtection(serverLevel, (LivingEntity) (Object) this, damagesource);
            } else {
                l = 0.0F;
            }

            if (l > 0.0F) {
                return -(f - CombatRules.getDamageAfterMagicAbsorb(f.floatValue(), l));
            }
            return -0.0;
        };
        float magicModifier = magic.apply((double) damage).floatValue();
        damage += magicModifier;

        Function<Double, Double> absorption = f -> -(Math.max(f - Math.max(f - this.getAbsorptionAmount(), 0.0F), 0.0F));
        float absorptionModifier = absorption.apply((double) damage).floatValue();

        EntityDamageEvent event = CraftEventFactory.handleLivingEntityDamageEvent((LivingEntity) (Object) this, damagesource, originalDamage, freezingModifier, hardHatModifier, blockingModifier, armorModifier, resistanceModifier, magicModifier, absorptionModifier, freezing, hardHat, blocking, armor, resistance, magic, absorption);
        if (damagesource.getEntity() instanceof net.minecraft.world.entity.player.Player) {
            ((net.minecraft.world.entity.player.Player) damagesource.getEntity()).resetAttackStrengthTicker();
        }

        if (event.isCancelled()) {
            DecorationOps.cancel().invoke(false);
            return;
        }

        damage = (float) event.getFinalDamage();
        float damageOffset = damage - originalDamage;
        float armorDamage = (float) (event.getDamage() + event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) + event.getDamage(EntityDamageEvent.DamageModifier.HARD_HAT));
        entityDamageResult = new

                EntityDamageResult(
                Math.abs(damageOffset) > 1E-6,
                originalDamage,
                damage,
                damageOffset,
                originalArmorDamage,
                armorDamage - originalArmorDamage,
                hardHatModifier > 0 && damage <= 0,
                armorModifier > 0 && (event.getDamage() + event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) + event.getDamage(EntityDamageEvent.DamageModifier.HARD_HAT)) <= 0,
                blockingModifier < 0 && event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) >= 0
        );

        if (damage > 0 || !human) {
            banner$damageResult = true;
        } else {
            if (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) < 0) {
                banner$damageResult = true;
            } else {
                banner$damageResult = originalDamage > 0;
            }
        }
        if (damage == 0) {
            originalDamage = 0;
            DecorationOps.blackhole().invoke(originalDamage);
        }
    }

    @ModifyExpressionValue(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    private boolean banner$cancelShieldBlock(boolean original) {
        return (entityDamageResult == null || !entityDamageResult.blockingCancelled()) && original;
    }

    @Decorate(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtHelmet(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void banner$cancelHurtHelmet(LivingEntity instance, DamageSource damageSource, float f) throws
            Throwable {
        if (entityDamageResult == null || !entityDamageResult.helmetHurtCancelled()) {
            var result = f + entityDamageResult.armorDamageOffset();
            if (entityDamageResult.armorDamageOffset() < 0 && result < 0) {
                result = f + f * (entityDamageResult.armorDamageOffset() / entityDamageResult.originalArmorDamage());
            }
            if (result > 0) {
                DecorationOps.callsite().invoke(instance, damageSource, result);
            }
        }
    }

    @Decorate(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I"),
            slice = @Slice(to = @At(value = "FIELD", target = "Lnet/minecraft/tags/DamageTypeTags;BYPASSES_COOLDOWN:Lnet/minecraft/tags/TagKey;")))
    private int banner$useInvulnerableDuration(LivingEntity instance) throws Throwable {
        int result = (int) DecorationOps.callsite().invoke(instance);
        return result + 10 - (int) (this.invulnerableDuration / 2.0F);
    }

    @Decorate(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void banner$returnIfBlocked(LivingEntity instance, DamageSource damageSource, float f) throws
            Throwable {
        DecorationOps.callsite().invoke(instance, damageSource, f);
        if (!banner$damageResult) {
            DecorationOps.cancel().invoke(false);
            return;
        }
        DecorationOps.blackhole().invoke();
    }

    @Override
    public boolean damageEntity0(DamageSource damagesource, float f) {
        if (!this.isInvulnerableTo(damagesource)) {
            final boolean human = ((LivingEntity) (Object) this) instanceof Player;
            if (f <= 0) return banner$damageResult = true;
            float originalDamage = f;
            Function<Double, Double> hardHat = new Function<>() {
                @Override
                public Double apply(Double f) {
                    if (damagesource.is(DamageTypeTags.DAMAGES_HELMET) && !getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                        return -(f - (f * 0.75F));

                    }
                    return -0.0;
                }
            };
            float hardHatModifier = hardHat.apply((double) f).floatValue();
            f += hardHatModifier;

            Function<Double, Double> blocking = new Function<>() {
                @Override
                public Double apply(Double f) {
                    return -((isDamageSourceBlocked(damagesource)) ? f : 0.0);
                }
            };
            float blockingModifier = blocking.apply((double) f).floatValue();
            f += blockingModifier;

            Function<Double, Double> armor = new Function<>() {
                @Override
                public Double apply(Double f) {
                    return -(f - getDamageAfterArmorAbsorb(damagesource, f.floatValue()));
                }
            };
            float armorModifier = armor.apply((double) f).floatValue();
            f += armorModifier;

            Function<Double, Double> resistance = new Function<>() {
                @Override
                public Double apply(Double f) {
                    if (!damagesource.is(DamageTypeTags.BYPASSES_EFFECTS) && hasEffect(MobEffects.DAMAGE_RESISTANCE) && !damagesource.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
                        int i = (((LivingEntity) (Object) this).getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
                        int j = 25 - i;
                        float f1 = f.floatValue() * (float) j;
                        return -(f - (f1 / 25.0F));
                    }
                    return -0.0;
                }
            };
            float resistanceModifier = resistance.apply((double) f).floatValue();
            f += resistanceModifier;

            Function<Double, Double> magic = new Function<>() {
                @Override
                public Double apply(Double f) {
                    return -(f - getDamageAfterMagicAbsorb(damagesource, f.floatValue()));
                }
            };
            float magicModifier = magic.apply((double) f).floatValue();
            f += magicModifier;

            Function<Double, Double> absorption = new Function<>() {
                @Override
                public Double apply(Double f) {
                    return -(Math.max(f - Math.max(f - getAbsorptionAmount(), 0.0F), 0.0F));
                }
            };
            float absorptionModifier = absorption.apply((double) f).floatValue();

            EntityDamageEvent event = CraftEventFactory.handleLivingEntityDamageEvent(this, damagesource, originalDamage, hardHatModifier, blockingModifier, armorModifier, resistanceModifier, magicModifier, absorptionModifier, null, blocking, armor, resistance, magic, absorption);
            if (damagesource.getEntity() instanceof Player) {
                ((Player) damagesource.getEntity()).resetAttackStrengthTicker(); // Moved from EntityHuman in order to make the cooldown reset get called after the damage event is fired
            }
            if (event.isCancelled()) {
                return banner$damageResult = false;
            }

            f = (float) event.getFinalDamage();

            // Resistance
            if (event.getDamage(EntityDamageEvent.DamageModifier.RESISTANCE) < 0) {
                float f3 = (float) -event.getDamage(EntityDamageEvent.DamageModifier.RESISTANCE);
                if (f3 > 0.0F && f3 < 3.4028235E37F) {
                    if (((LivingEntity) (Object) this) instanceof ServerPlayer) {
                        ((ServerPlayer) (Object) this).awardStat(Stats.DAMAGE_RESISTED, Math.round(f3 * 10.0F));
                    } else if (damagesource.getEntity() instanceof ServerPlayer) {
                        ((ServerPlayer) damagesource.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f3 * 10.0F));
                    }
                }
            }

            // Apply damage to helmet
            if (damagesource.is(DamageTypeTags.DAMAGES_HELMET) && !this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                this.hurtHelmet(damagesource, f);
            }

            // Apply damage to armor
            if (!damagesource.is(DamageTypeTags.BYPASSES_ARMOR)) {
                float armorDamage = (float) (event.getDamage() + event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) + event.getDamage(EntityDamageEvent.DamageModifier.HARD_HAT));
                this.hurtArmor(damagesource, armorDamage);
            }

            // Apply blocking code // PAIL: steal from above
            if (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) < 0) {
                this.level().broadcastEntityEvent(this, (byte) 29); // SPIGOT-4635 - shield damage sound
                this.hurtCurrentlyUsedShield((float) -event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING));
                Entity entity = damagesource.getDirectEntity();

                if (entity instanceof LivingEntity) {
                    this.blockUsingShield((LivingEntity) entity);
                }
            }

            absorptionModifier = (float) -event.getDamage(EntityDamageEvent.DamageModifier.ABSORPTION);
            this.setAbsorptionAmount(Math.max(this.getAbsorptionAmount() - absorptionModifier, 0.0F));
            float f2 = absorptionModifier;

            if (f2 > 0.0F && f2 < 3.4028235E37F && ((LivingEntity) (Object) this) instanceof Player) {
                ((Player) (Object) this).awardStat(Stats.DAMAGE_ABSORBED, Math.round(f2 * 10.0F));
            }
            if (f2 > 0.0F && f2 < 3.4028235E37F) {
                Entity entity = damagesource.getEntity();

                if (entity instanceof ServerPlayer entityplayer) {

                    entityplayer.awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f2 * 10.0F));
                }
            }

            if (f > 0 || !human) {
                if (human) {
                    // PAIL: Be sure to drag all this code from the EntityHuman subclass each update.
                    ((Player) (Object) this).causeFoodExhaustion(damagesource.getFoodExhaustion(), org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.DAMAGED); // CraftBukkit - EntityExhaustionEvent
                    if (f < 3.4028235E37F) {
                        ((Player) (Object) this).awardStat(Stats.DAMAGE_TAKEN, Math.round(f * 10.0F));
                    }
                }
                // CraftBukkit end
                float f3 = this.getHealth();

                this.getCombatTracker().recordDamage(damagesource, f);
                this.setHealth(f3 - f);
                // CraftBukkit start
                if (!human) {
                    this.setAbsorptionAmount(this.getAbsorptionAmount() - f);
                }
                this.gameEvent(GameEvent.ENTITY_DAMAGE);

                return banner$damageResult = true;
            } else {
                // Duplicate triggers if blocking
                if (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) < 0) {
                    if (((LivingEntity) (Object) this)instanceof ServerPlayer) {
                        CriteriaTriggers.ENTITY_HURT_PLAYER.trigger(((ServerPlayer) (Object) this), damagesource, f, originalDamage, true);
                        f2 = (float) -event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING);
                        if (f2 > 0.0F && f2 < 3.4028235E37F) {
                            ((ServerPlayer) (Object) this).awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(originalDamage * 10.0F));
                        }
                    }

                    if (damagesource.getEntity() instanceof ServerPlayer) {
                        CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer) damagesource.getEntity(), this, damagesource, f, originalDamage, true);
                    }

                    return banner$damageResult = false;
                } else {
                    return banner$damageResult = originalDamage > 0;
                }
                // CraftBukkit end
            }
        }
        return banner$damageResult = false; // CraftBukkit
    }

    private transient EntityRegainHealthEvent.RegainReason banner$regainReason;

    @Override
    public void heal(float healAmount, EntityRegainHealthEvent.RegainReason regainReason) {
        pushHealReason(regainReason);
        this.heal(healAmount);
    }

    @Override
    public void pushHealReason(EntityRegainHealthEvent.RegainReason reason) {
        banner$regainReason = reason;
    }

    @Redirect(method = "heal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"))
    public void banner$healEvent(LivingEntity livingEntity, float health) {
        EntityRegainHealthEvent.RegainReason regainReason = banner$regainReason == null ? EntityRegainHealthEvent.RegainReason.CUSTOM : banner$regainReason;
        banner$regainReason = null;
        float f = this.getHealth();
        float amount = health - f;
        EntityRegainHealthEvent event = new EntityRegainHealthEvent(this.getBukkitEntity(), amount, regainReason);
        if (this.bridge$valid()) {
            Bukkit.getPluginManager().callEvent(event);
        }

        if (!event.isCancelled()) {
            this.setHealth(this.getHealth() + (float) event.getAmount());
        }
    }

    @Inject(method = "heal", at = @At(value = "RETURN"))
    public void banner$resetReason(float healAmount, CallbackInfo ci) {
        banner$regainReason = null;
    }

    @Redirect(method = "die",
            at = @At(value = "INVOKE",
                    target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
                    remap = false))
    private void banner$logNamedDeaths(Logger instance, String s, Object o1, Object o2) {
        if (org.spigotmc.SpigotConfig.logNamedDeaths) LOGGER.info("Named entity {} died: {}", ((LivingEntity) (Object) this), this.getCombatTracker().getDeathMessage().getString()); // Spigot
    }

    @Override
    public boolean addEffect(MobEffectInstance effect, EntityPotionEffectEvent.Cause cause) {
        pushEffectCause(cause);
        return this.addEffect(effect, (Entity) null);
    }

    @Override
    public boolean removeAllEffects(EntityPotionEffectEvent.Cause cause) {
        pushEffectCause(cause);
        return this.removeAllEffects();
    }


    // Banner start - throw out for Mod Mixins
    net.minecraft.world.item.ItemStack banner$itemstack = null;
    net.minecraft.world.item.ItemStack banner$itemstack1 = ItemStack.EMPTY;
    // Banner end

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    private boolean checkTotemDeathProtection(DamageSource damageSourceIn) {
        if (damageSourceIn.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        } else {
            org.bukkit.inventory.EquipmentSlot bukkitHand = null;
            for (InteractionHand hand : InteractionHand.values()) {
                banner$itemstack1 = this.getItemInHand(hand);
                if (banner$itemstack1.is(Items.TOTEM_OF_UNDYING)) {
                    banner$itemstack = banner$itemstack1.copy();
                    bukkitHand = CraftEquipmentSlot.getHand(hand);
                    // Banner start - remain for Mods Mixin(Not any function)
                    if (bukkitHand.equals("NULL")) {
                        banner$itemstack1.shrink(1);
                    }
                    // Banner end
                    // itemstack1.shrink(1);
                    break;
                }
            }

            EntityResurrectEvent event = new EntityResurrectEvent((org.bukkit.entity.LivingEntity) this.getBukkitEntity(), bukkitHand);
            event.setCancelled(banner$itemstack == null);
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                if (!banner$itemstack1.isEmpty()) {
                    banner$itemstack1.shrink(1);
                }
                if (banner$itemstack != null && (Object) this instanceof ServerPlayer serverplayerentity) {
                    serverplayerentity.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                    CriteriaTriggers.USED_TOTEM.trigger(serverplayerentity, banner$itemstack);
                }

                this.setHealth(1.0F);
                this.removeAllEffects(EntityPotionEffectEvent.Cause.TOTEM);
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1), EntityPotionEffectEvent.Cause.TOTEM);
                pushEffectCause(EntityPotionEffectEvent.Cause.TOTEM);
                this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1), EntityPotionEffectEvent.Cause.TOTEM);
                this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 1), EntityPotionEffectEvent.Cause.TOTEM);
                this.level().broadcastEntityEvent((Entity) (Object) this, (byte) 35);
            }
            return !event.isCancelled();
        }
    }

    @Inject(method = "createWitherRose", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$witherRoseDrop(LivingEntity livingEntity, CallbackInfo ci, @Local ItemEntity
            itemEntity) {
        org.bukkit.event.entity.EntityDropItemEvent event = new org.bukkit.event.entity.EntityDropItemEvent(this.getBukkitEntity(), (org.bukkit.entity.Item) itemEntity.getBukkitEntity());
        CraftEventFactory.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "createWitherRose", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean banner$fireWitherRoseForm(Level instance, BlockPos pPos, BlockState pNewState, int pFlags) {
        return CraftEventFactory.handleBlockFormEvent(instance, pPos, pNewState, 3, (Entity) (Object) this);
    }

    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setSharedFlag(IZ)V"))
    public void banner$stopGlide(Vec3 travelVector, CallbackInfo ci) {
        BukkitSnapshotCaptures.capturebanner$stopGlide(true);
    }

    @Redirect(method = "updateFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setSharedFlag(IZ)V"))
    public void banner$toggleGlide(LivingEntity livingEntity, int flag, boolean set) {
        if (set != livingEntity.getSharedFlag(flag) && !CraftEventFactory.callToggleGlideEvent(livingEntity, set).isCancelled()) {
            livingEntity.setSharedFlag(flag, set);
        }
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public boolean isPickable() {
        return !this.isRemoved() && this.collides;
    }

    @Eject(method = "completeUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack banner$itemConsume(ItemStack itemStack, Level worldIn, LivingEntity
            entityLiving, CallbackInfo ci) {
        if (((LivingEntity)(Object) this) instanceof ServerPlayer) {
            final org.bukkit.inventory.ItemStack craftItem = CraftItemStack.asBukkitCopy(itemStack);
            final PlayerItemConsumeEvent event = new PlayerItemConsumeEvent((org.bukkit.entity.Player)this.getBukkitEntity(), craftItem, CraftEquipmentSlot.getHand(this.getUsedItemHand()));
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                ((ServerPlayer) (Object) this).getBukkitEntity().updateInventory();
                ((ServerPlayer) (Object) this).getBukkitEntity().updateScaledHealth();
                ci.cancel();
                return null;
            } else if (!craftItem.equals(event.getItem())) {
                return CraftItemStack.asNMSCopy(event.getItem()).finishUsingItem(worldIn, entityLiving);
            }
        }
        return itemStack.finishUsingItem(worldIn, entityLiving);
    }

    @Eject(method = "randomTeleport", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/entity/LivingEntity;teleportTo(DDD)V"))
    private void banner$entityTeleport(LivingEntity entity, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        EntityTeleportEvent event = new EntityTeleportEvent(getBukkitEntity(), new Location(this.level().getWorld(), this.getX(), this.getY(), this.getZ()), new Location(this.level().getWorld(), x, y, z));
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            this.teleportTo(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
        } else {
            this.teleportTo(this.getX(), this.getY(), this.getZ());
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "addEatEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    public void banner$foodEffectCause(FoodProperties foodProperties, CallbackInfo ci) {
        ((LivingEntity) (Object) this).pushEffectCause(EntityPotionEffectEvent.Cause.FOOD);
    }

    @Inject(method = "setArrowCount", cancellable = true, at = @At("HEAD"))
    private void banner$onArrowChange(int count, CallbackInfo ci) {
        if (banner$callArrowCountChange(count, false)) {
            ci.cancel();
        }
    }

    @Override
    public void pushEffectCause(EntityPotionEffectEvent.Cause cause) {
        this.banner$cause = cause;
    }

    @Inject(method = "collectEquipmentChanges", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;equipmentHasChanged(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
    ))
    private void banner$fireArmorChangeEvent(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir, @Local EquipmentSlot equipmentSlot, @Local(ordinal = 0) ItemStack itemStack, @Local(ordinal = 1) ItemStack itemStack2) {
        // Paper start - PlayerArmorChangeEvent
        if (((LivingEntity) (Object) this) instanceof ServerPlayer && equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            final org.bukkit.inventory.ItemStack oldItem = CraftItemStack.asBukkitCopy(itemStack);
            final org.bukkit.inventory.ItemStack newItem = CraftItemStack.asBukkitCopy(itemStack2);
            new PlayerArmorChangeEvent((org.bukkit.entity.Player)this.getBukkitEntity(), PlayerArmorChangeEvent.SlotType.valueOf(equipmentSlot.name()), oldItem, newItem).callEvent();
        }
    }

    @Override
    public final void setArrowCount(int count, boolean reset) {
        if (banner$callArrowCountChange(count, reset)) {
            return;
        }
        this.entityData.set(DATA_ARROW_COUNT_ID, count);
    }

    private boolean banner$callArrowCountChange(int newCount, boolean reset) {
        return CraftEventFactory.callArrowBodyCountChangeEvent((LivingEntity) (Object) this, this.getArrowCount(), newCount, reset).isCancelled();
    }

    @Override
    public void equipEventAndSound(EquipmentSlot slot, ItemStack oldItem, ItemStack newItem, boolean silent) {
        boolean flag = newItem.isEmpty() && oldItem.isEmpty();
        if (!flag && !ItemStack.isSameItem(oldItem, newItem) && !this.firstTick) {
            Equipable equipable = Equipable.get(newItem);
            if (equipable != null && !this.isSpectator() && equipable.getEquipmentSlot() == slot) {
                if (!this.level().isClientSide() && !this.isSilent() && !silent) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), equipable.getEquipSound().value(), this.getSoundSource(), 1.0F, 1.0F);
                }

                if (this.doesEmitEquipEvent(slot)) {
                    this.gameEvent(GameEvent.EQUIP);
                }
            }

        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slotIn, ItemStack stack, boolean silent) {
        this.setItemSlot(slotIn, stack, silent);
    }

    @Override
    public void onEquipItem(EquipmentSlot enumitemslot, ItemStack itemstack, ItemStack itemstack1, boolean silent) {
        banner$silent.set(silent);
        this.onEquipItem(enumitemslot, itemstack, itemstack1);
    }

    @Override
    public SoundEvent getHurtSound0(DamageSource damagesource) {
        return InjectionLivingEntity.super.getHurtSound0(damagesource);
    }

    @Override
    public SoundEvent getDeathSound0() {
        return getDeathSound();
    }

    @Override
    public SoundEvent getFallDamageSound0(int fallHeight) {
        return getFallDamageSound(fallHeight);
    }

    @Override
    public SoundEvent getDrinkingSound0(net.minecraft.world.item.ItemStack itemstack) {
        return getDrinkingSound(itemstack);
    }

    @Override
    public SoundEvent getEatingSound0(net.minecraft.world.item.ItemStack itemstack) {
        return getEatingSound(itemstack);
    }

    @Override
    public int bridge$expToDrop() {
        return expToDrop;
    }

    @Override
    public void banner$setExpToDrop(int expToDrop) {
        this.expToDrop = expToDrop;
    }

    @Override
    public boolean bridge$forceDrops() {
        return forceDrops;
    }

    @Override
    public void banner$setForceDrops(boolean forceDrops) {
        this.forceDrops = forceDrops;
    }

    @Override
    public ArrayList<org.bukkit.inventory.ItemStack> bridge$drops() {
        return drops;
    }

    @Override
    public void banner$setDrops(ArrayList<org.bukkit.inventory.ItemStack> drops) {
        this.drops = drops;
    }

    @Override
    public CraftAttributeMap bridge$craftAttributes() {
        return craftAttributes;
    }

    @Override
    public void banner$setCraftAttributes(CraftAttributeMap craftAttributes) {
        this.craftAttributes = craftAttributes;
    }

    @Override
    public boolean bridge$collides() {
        return collides;
    }

    @Override
    public void banner$setCollides(boolean collides) {
        this.collides = collides;
    }

    @Override
    public Set<UUID> bridge$collidableExemptions() {
        return collidableExemptions;
    }

    @Override
    public void banner$setCollidableExemptions(Set<UUID> collidableExemptions) {
        this.collidableExemptions = collidableExemptions;
    }

    @Override
    public boolean bridge$bukkitPickUpLoot() {
        return bukkitPickUpLoot;
    }

    @Override
    public void banner$setBukkitPickUpLoot(boolean bukkitPickUpLoot) {
        this.bukkitPickUpLoot = bukkitPickUpLoot;
    }

    @Override
    public boolean bridge$isTickingEffects() {
        return isTickingEffects;
    }

    @Override
    public void banner$setIsTickingEffects(boolean isTickingEffects) {
        this.isTickingEffects = isTickingEffects;
    }

    @Override
    public List<ProcessableEffect> bridge$effectsToProcess() {
        return effectsToProcess;
    }

    @Override
    public void banner$setEffectsToProcess(List<ProcessableEffect> effectsToProcess) {
        this.effectsToProcess = effectsToProcess;
    }

    @Override
    public float getBukkitYaw() {
        return getYHeadRot();
    }

    @Override
    public Optional<EntityPotionEffectEvent.Cause> getEffectCause() {
        try {
            return Optional.ofNullable(banner$cause);
        } finally {
            banner$cause = null;
        }
    }
}