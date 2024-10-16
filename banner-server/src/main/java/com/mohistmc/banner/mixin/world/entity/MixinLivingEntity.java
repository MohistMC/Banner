package com.mohistmc.banner.mixin.world.entity;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import com.mohistmc.banner.bukkit.ProcessableEffect;
import com.mohistmc.banner.injection.world.entity.InjectionLivingEntity;
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
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    /**
     * @author wdog5
     * @reason
     */
    /*
    @Overwrite
    protected void tickEffects() {
        this.isTickingEffects = true;
        Iterator<MobEffect> iterator = this.activeEffects.keySet().iterator();

        try {
            while (iterator.hasNext()) {
                MobEffect effect = iterator.next();
                MobEffectInstance effectinstance = this.activeEffects.get(effect);
                if (!effectinstance.tick((LivingEntity) (Object) this, () -> {
                    onEffectUpdated(effectinstance, true, null);
                })) {
                    if (!this.level().isClientSide) {

                        EntityPotionEffectEvent event = CraftEventFactory.callEntityPotionEffectChangeEvent((LivingEntity) (Object) this, effectinstance, null, EntityPotionEffectEvent.Cause.EXPIRATION);
                        if (event.isCancelled()) {
                            continue;
                        }

                        iterator.remove();
                        this.onEffectRemoved(effectinstance);
                    }
                } else if (effectinstance.getDuration() % 600 == 0) {
                    this.onEffectUpdated(effectinstance, false, null);
                }
            }
        } catch (ConcurrentModificationException ignored) {
        }

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

        if (this.effectsDirty) {
            if (!this.level().isClientSide) {
                this.updateInvisibilityStatus();
            }

            this.effectsDirty = false;
        }

        int i = this.entityData.get(DATA_EFFECT_COLOR_ID);
        boolean flag1 = this.entityData.get(DATA_EFFECT_AMBIENCE_ID);
        if (i > 0) {
            boolean flag;
            if (this.isInvisible()) {
                flag = this.random.nextInt(15) == 0;
            } else {
                flag = this.random.nextBoolean();
            }

            if (flag1) {
                flag &= this.random.nextInt(5) == 0;
            }

            if (flag && i > 0) {
                double d0 = (double) (i >> 16 & 255) / 255.0D;
                double d1 = (double) (i >> 8 & 255) / 255.0D;
                double d2 = (double) (i >> 0 & 255) / 255.0D;
                this.level().addParticle(flag1 ? ParticleTypes.A : ParticleTypes.ENTITY_EFFECT, this.getX() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(), this.getY() + this.random.nextDouble() * (double) this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(), d0, d1, d2);
            }
        }
    }*/

    // @Shadow public abstract boolean addEffect(MobEffectInstance effectInstanceIn, Entity entity);

    // Banner - fix mixin(locals = LocalCapture.CAPTURE_FAILHARD)
    public EntityPotionEffectEvent.Cause cause;
    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public boolean addEffect(MobEffectInstance effectInstanceIn, Entity entity) {
        cause = getEffectCause().orElse(EntityPotionEffectEvent.Cause.UNKNOWN);
        if (isTickingEffects) {
            effectsToProcess.add(new ProcessableEffect(effectInstanceIn, cause));
            return true;
        }

        if (!this.canBeAffected(effectInstanceIn)) {
            return false;
        } else {
            MobEffectInstance effectinstance = this.activeEffects.get(effectInstanceIn.getEffect());
            boolean flag = false;

            boolean override = false;
            if (effectinstance != null) {
                override = new MobEffectInstance(effectinstance).update(effectInstanceIn);
            }

            EntityPotionEffectEvent event = CraftEventFactory.callEntityPotionEffectChangeEvent((LivingEntity) (Object) this, effectinstance, effectInstanceIn, cause, override);
            override = event.isOverride();
            if (event.isCancelled()) {
                return false;
            }

            if (effectinstance == null) {
                this.activeEffects.put(effectInstanceIn.getEffect(), effectInstanceIn);
                this.onEffectAdded(effectInstanceIn, entity);
                flag = true;
                effectInstanceIn.onEffectAdded((LivingEntity) (Object) this);
            } else if (override) {
                effectinstance.update(effectInstanceIn);
                this.onEffectUpdated(effectinstance, true, entity);
                flag = true;
            }

            effectInstanceIn.onEffectStarted((LivingEntity) (Object) this);
            return flag;
        }
    }

    @SuppressWarnings("unused") // mock
    /*
    public MobEffectInstance c(@Nullable MobEffect potioneffectin, EntityPotionEffectEvent.Cause cause) {
        pushEffectCause(cause);
        return removeEffectNoUpdate(potioneffectin);
    }*/

    @Inject(method = "removeEffectNoUpdate", cancellable = true, at = @At("HEAD"))
    public void banner$clearActive(Holder<MobEffect> holder, CallbackInfoReturnable<MobEffectInstance> cir) {
        EntityPotionEffectEvent.Cause cause = getEffectCause().orElse(EntityPotionEffectEvent.Cause.UNKNOWN);
        if (isTickingEffects) {
            effectsToProcess.add(new ProcessableEffect(holder.value(), cause));
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

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else if (this.level().isClientSide) {
            return false;
        } else if (this.isDeadOrDying() || this.isRemoved() || this.getHealth() <= 0.0F) {
            return false;
        } else if (source.is(DamageTypeTags.IS_FIRE) && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        } else {
            if (this.isSleeping() && !this.level().isClientSide) {
                this.stopSleeping();
            }

            this.noActionTime = 0;
            float f = amount;
            boolean flag = f > 0.0F && this.isDamageSourceBlocked(source); // Copied from below
            float f1 = 0.0F;
            // ShieldBlockEvent implemented in damageEntity0

            if (false && amount > 0.0F && this.isDamageSourceBlocked(source)) {
                this.hurtCurrentlyUsedShield(amount);
                f1 = amount;
                amount = 0.0F;
                if (!source.is(DamageTypeTags.IS_PROJECTILE)) {
                    Entity entity = source.getDirectEntity();
                    if (entity instanceof LivingEntity) {
                        this.blockUsingShield((LivingEntity) entity);
                    }
                }

                flag = true;
            }

            if (source.is(DamageTypeTags.IS_FREEZING) && this.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
                f *= 5.0F;
            }

            this.walkAnimation.setSpeed(1.5F);
            boolean flag1 = true;
            if ((float) this.invulnerableTime > (float) this.invulnerableDuration / 2.0F && !source.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
                if (amount <= this.lastHurt) {
                    return false;
                }

                this.actuallyHurt(source, amount - this.lastHurt);
                if (!banner$damageResult) {
                    return false;
                }
                this.lastHurt = amount;
                flag1 = false;
            } else {
                this.actuallyHurt(source, amount);
                if (!banner$damageResult) {
                    return false;
                }
                this.lastHurt = amount;
                this.invulnerableTime = 20;
                this.hurtDuration = 10;
                this.hurtTime = this.hurtDuration;
            }

            if ((Object) this instanceof Animal) {
                ((Animal) (Object) this).resetLove();
                if ((Object) this instanceof TamableAnimal) {
                    ((TamableAnimal) (Object) this).setOrderedToSit(false);
                }
            }

            Entity entity1 = source.getEntity();
            if (entity1 != null) {
                if (entity1 instanceof LivingEntity && !source.is(DamageTypeTags.NO_ANGER)) {
                    this.setLastHurtByMob((LivingEntity) entity1);
                }

                if (entity1 instanceof net.minecraft.world.entity.player.Player) {
                    this.lastHurtByPlayerTime = 100;
                    this.lastHurtByPlayer = (net.minecraft.world.entity.player.Player) entity1;
                } else if (entity1 instanceof TamableAnimal wolfentity) {
                    if (wolfentity.isTame()) {
                        this.lastHurtByPlayerTime = 100;
                        LivingEntity livingentity = wolfentity.getOwner();
                        if (livingentity instanceof net.minecraft.world.entity.player.Player) {
                            this.lastHurtByPlayer = (net.minecraft.world.entity.player.Player) livingentity;
                        } else {
                            this.lastHurtByPlayer = null;
                        }
                    }
                }
            }

            if (flag1) {
                if (flag) {
                    this.level().broadcastEntityEvent((LivingEntity) (Object) this, (byte) 29);
                } else {
                    this.level().broadcastDamageEvent((LivingEntity) (Object) this, source);
                }

                if (!source.is(DamageTypeTags.NO_IMPACT) && (!flag || amount > 0.0F)) {
                    this.markHurt();
                }

                if (entity1 != null && !source.is(DamageTypeTags.NO_KNOCKBACK)) {
                    double d1 = entity1.getX() - this.getX();

                    double d0;
                    for (d0 = entity1.getZ() - this.getZ(); d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
                        d1 = (Math.random() - Math.random()) * 0.01D;
                    }

                    this.knockback(0.4F, d1, d0);
                    if (!flag) {
                        this.indicateDamage(d1, d0);
                    }
                }
            }

            if (this.isDeadOrDying()) {
                if (!this.checkTotemDeathProtection(source)) {
                    SoundEvent soundevent = this.getDeathSound();
                    if (flag1 && soundevent != null) {
                        this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
                    }

                    this.die(source);
                }
            } else if (flag1) {
                this.playHurtSound(source);
            }

            boolean flag2 = !flag || amount > 0.0F;
            if (flag2) {
                this.lastDamageSource = source;
                this.lastDamageStamp = this.level().getGameTime();
            }

            if ((Object) this instanceof ServerPlayer) {
                CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer) (Object) this, source, f, amount, flag);
                if (f1 > 0.0F && f1 < 3.4028235E37F) {
                    ((ServerPlayer) (Object) this).awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(f1 * 10.0F));
                }
            }

            if (entity1 instanceof ServerPlayer) {
                CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer) entity1, (LivingEntity) (Object) this, source, f, amount, flag);
            }

            return flag2;
        }
    }

    @Inject(method = "actuallyHurt", cancellable = true, at = @At("HEAD"))
    public void banner$redirectDamageEntity(DamageSource damageSrc, float damageAmount, CallbackInfo ci) {
        damageEntity0(damageSrc, damageAmount);
        ci.cancel();
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

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    private boolean checkTotemDeathProtection(DamageSource damageSourceIn) {
        if (damageSourceIn.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        } else {
            net.minecraft.world.item.ItemStack itemstack = null;

            net.minecraft.world.item.ItemStack itemstack1 = ItemStack.EMPTY;
            org.bukkit.inventory.EquipmentSlot bukkitHand = null;
            for (InteractionHand hand : InteractionHand.values()) {
                itemstack1 = this.getItemInHand(hand);
                if (itemstack1.is(Items.TOTEM_OF_UNDYING)) {
                    itemstack = itemstack1.copy();
                    bukkitHand = CraftEquipmentSlot.getHand(hand);
                    // itemstack1.shrink(1);
                    break;
                }
            }

            EntityResurrectEvent event = new EntityResurrectEvent((org.bukkit.entity.LivingEntity) this.getBukkitEntity(), bukkitHand);
            event.setCancelled(itemstack == null);
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                if (!itemstack1.isEmpty()) {
                    itemstack1.shrink(1);
                }
                if (itemstack != null && (Object) this instanceof ServerPlayer serverplayerentity) {
                    serverplayerentity.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                    CriteriaTriggers.USED_TOTEM.trigger(serverplayerentity, itemstack);
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