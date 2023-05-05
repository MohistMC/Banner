package com.mohistmc.banner.mixin.world.entity;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mohistmc.banner.bukkit.ProcessableEffect;
import com.mohistmc.banner.injection.world.entity.InjectionLivingEntity;
import net.minecraft.core.BlockPos;
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
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_19_R3.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.jetbrains.annotations.Nullable;
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
import java.util.concurrent.atomic.AtomicReference;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements InjectionLivingEntity {

    @Shadow @Final public static EntityDataAccessor<Float> DATA_HEALTH_ID;

    @Shadow public abstract double getAttributeValue(Attribute attribute);

    @Shadow @Final private AttributeMap attributes;

    @Shadow public abstract SoundEvent getEatingSound(net.minecraft.world.item.ItemStack stack);

    @Shadow protected abstract SoundEvent getDrinkingSound(net.minecraft.world.item.ItemStack stack);

    @Shadow protected abstract SoundEvent getFallDamageSound(int height);

    @Shadow @Nullable protected abstract SoundEvent getDeathSound();

    @Shadow public abstract void onEquipItem(EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2);

    @Shadow @Nullable public abstract AttributeInstance getAttribute(Attribute attribute);

    public MixinLivingEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public int expToDrop;
    public boolean forceDrops;
    public ArrayList<org.bukkit.inventory.ItemStack> drops = new ArrayList<org.bukkit.inventory.ItemStack>();
    public org.bukkit.craftbukkit.v1_19_R3.attribute.CraftAttributeMap craftAttributes;
    public boolean collides = true;
    public Set<UUID> collidableExemptions = new HashSet<>();
    public boolean bukkitPickUpLoot;
    private boolean isTickingEffects = false;
    private List<ProcessableEffect> effectsToProcess = Lists.newArrayList();

    // Banner - add fields
    private AtomicReference<BlockState> banner$FallState = new AtomicReference<>();
    private AtomicReference<Boolean> banner$silent = new AtomicReference<>();

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
    private <T extends ParticleOptions>  int banner$addCheckFall(ServerLevel instance,  T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed) {
        // CraftBukkit start - visiblity api
        float banner$f = (float) Mth.ceil(this.fallDistance - 3.0F);
        double banner$d = Math.min((double)(0.2F + banner$f / 15.0F), 2.5);
        int banner$i = (int)(150.0 * banner$d);
        if (((LivingEntity) (Object) this) instanceof ServerPlayer) {
            return ((ServerLevel) this.level).sendParticles((ServerPlayer) (Object) this, new BlockParticleOption(ParticleTypes.BLOCK, banner$FallState.get()), this.getX(), this.getY(), this.getZ(), banner$i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, false);
        } else {
            return ((ServerLevel) this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, banner$FallState.get()), this.getX(), this.getY(), this.getZ(), banner$i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D);
        }
    }
    @ModifyExpressionValue(method = "onEquipItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z"))
    private boolean banner$addSilentCheck(EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2, CallbackInfo ci) {
        return !this.level.isClientSide() && !banner$silent.get();
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
        if (((LivingEntity) (Object) this) instanceof ServerPlayer && ((ServerPlayer) (Object) this).banner$initialized()) {
            CraftPlayer player = ((ServerPlayer) (Object) this).getBukkitEntity();

            double realHealth = Mth.clamp(health, 0.0F, player.getMaxHealth());
            player.setRealHealth(realHealth);

            player.updateScaledHealth(false);
            player.setRealHealth(realHealth);
            ci.cancel();
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
}
