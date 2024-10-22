package com.mohistmc.banner.mixin.world.entity;

import com.mohistmc.banner.BannerMod;
import com.mohistmc.banner.injection.world.entity.InjectionMob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Banner - TODO fix patches
@Mixin(Mob.class)
public abstract class MixinMob extends LivingEntity implements InjectionMob {

    @Shadow private boolean persistenceRequired;

    @Shadow @Nullable protected abstract SoundEvent getAmbientSound();

    @Shadow @Nullable private LivingEntity target;

    @Shadow @Nullable public abstract LivingEntity getTarget();

    @Shadow protected abstract boolean canReplaceCurrentItem(ItemStack candidate, ItemStack existing);

    @Shadow public abstract boolean canHoldItem(ItemStack stack);

    @Shadow protected abstract float getEquipmentDropChance(EquipmentSlot slot);

    @Shadow protected abstract void setItemSlotAndDropWhenKilled(EquipmentSlot slot, ItemStack stack);
    @Shadow @Nullable public abstract <T extends Mob> T convertTo(EntityType<T> entityType, boolean bl);

    @Shadow @Nullable private Leashable.LeashData leashData;
    public boolean aware = true; // CraftBukkit

    protected transient boolean banner$targetSuccess = false;
    private transient EntityTargetEvent.TargetReason banner$reason;
    private transient boolean banner$fireEvent;
    private transient ItemEntity banner$item;
    private transient EntityTransformEvent.TransformReason banner$transform;

    protected MixinMob(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "setCanPickUpLoot", at = @At("HEAD"))
    public void banner$setPickupLoot(boolean canPickup, CallbackInfo ci) {
        super.banner$setBukkitPickUpLoot(canPickup);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public boolean canPickUpLoot() {
        return super.bridge$bukkitPickUpLoot();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(EntityType<? extends Mob> type, net.minecraft.world.level.Level worldIn, CallbackInfo ci) {
        this.aware = true;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void banner$setAware(CompoundTag compound, CallbackInfo ci) {
        compound.putBoolean("Bukkit.Aware", this.aware);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void banner$readAware(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("Bukkit.Aware")) {
            this.aware = compound.getBoolean("Bukkit.Aware");
        }
    }

    @Redirect(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setCanPickUpLoot(Z)V"))
    public void banner$setIfTrue(Mob mobEntity, boolean canPickup) {
        if (canPickup) mobEntity.setCanPickUpLoot(true);
    }

    @Inject(method = "serverAiStep", cancellable = true, at = @At("HEAD"))
    private void banner$unaware(CallbackInfo ci) {
        if (!this.aware) {
            ++this.noActionTime;
            ci.cancel();
        }
    }


    @Inject(method = "pickUpItem", at = @At("HEAD"))
    private void banner$captureItemEntity(ItemEntity itemEntity, CallbackInfo ci) {
        banner$item = itemEntity;
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void setTarget(@Nullable LivingEntity livingEntity) {
        boolean fireEvent = banner$fireEvent;
        banner$fireEvent = false;
        EntityTargetEvent.TargetReason reason = banner$reason == null ? EntityTargetEvent.TargetReason.UNKNOWN : banner$reason;
        banner$reason = null;
        if (getTarget() == livingEntity) {
            banner$targetSuccess = false;
            return;
        }
        if (fireEvent) {
            if (reason == EntityTargetEvent.TargetReason.UNKNOWN && this.getTarget() != null && livingEntity == null) {
                reason = (this.getTarget().isAlive() ? EntityTargetEvent.TargetReason.FORGOT_TARGET : EntityTargetEvent.TargetReason.TARGET_DIED);
            }
            if (reason == EntityTargetEvent.TargetReason.UNKNOWN) {
                BannerMod.LOGGER.warn("Unknown target reason setting {} target to {}", this, livingEntity);
            }
            CraftLivingEntity ctarget = null;
            if (livingEntity != null) {
                ctarget = (CraftLivingEntity) livingEntity.getBukkitEntity();
            }
            EntityTargetLivingEntityEvent event = new EntityTargetLivingEntityEvent(this.getBukkitEntity(), ctarget, reason);            Bukkit.getPluginManager().callEvent(event);
            level().getCraftServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                banner$targetSuccess = false;
                return;
            }
            if (event.getTarget() != null) {
                livingEntity = ((CraftLivingEntity) event.getTarget()).getHandle();
            } else {
                livingEntity = null;
            }
        }
        this.target = livingEntity;
        banner$targetSuccess = true;
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public ItemStack equipItemIfPossible(ItemStack stack) {
        ItemEntity itemEntity = banner$item;
        banner$item = null;
        EquipmentSlot equipmentslottype = getEquipmentSlotForItem(stack);
        ItemStack itemstack = this.getItemBySlot(equipmentslottype);
        boolean flag = this.canReplaceCurrentItem(stack, itemstack);

        if (equipmentslottype.isArmor() && !flag) {
            equipmentslottype = EquipmentSlot.MAINHAND;
            itemstack = this.getItemBySlot(equipmentslottype);
            flag = itemstack.isEmpty();
        }

        boolean canPickup = flag && this.canHoldItem(stack);
        if (itemEntity != null) {
            canPickup = !CraftEventFactory.callEntityPickupItemEvent((Mob) (Object) this, itemEntity, 0, !canPickup).isCancelled();
        }
        if (canPickup) {
            double d0 = this.getEquipmentDropChance(equipmentslottype);
            if (!itemstack.isEmpty() && (double) Math.max(this.random.nextFloat() - 0.1F, 0.0F) < d0) {
                banner$setForceDrops(true);
                this.spawnAtLocation(itemstack);
                banner$setForceDrops(false);
            }

            if (equipmentslottype.isArmor() && stack.getCount() > 1) {
                ItemStack itemstack1 = stack.copyWithCount(1);
                this.setItemSlotAndDropWhenKilled(equipmentslottype, itemstack1);
                return itemstack1;
            } else {
                this.setItemSlotAndDropWhenKilled(equipmentslottype, stack);
                return stack;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    // Banner TODO fixme
    @Inject(method = "interact", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;checkAndHandleImportantInteractions(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private void banner$unleash(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (CraftEventFactory.callPlayerUnleashEntityEvent((Mob) (Object) this, player, hand).isCancelled() && this.leashData != null) {
            ((ServerPlayer) player).connection.send(new ClientboundSetEntityLinkPacket((Mob) (Object) this, this.leashData.leashHolder));
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    /*@Inject(method = "checkAndHandleImportantInteractions", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setLeashedTo(Lnet/minecraft/world/entity/Entity;Z)V"))
    private void banner$leash(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (CraftEventFactory.callPlayerLeashEntityEvent((Mob) (Object) this, player, player, hand).isCancelled()) {
            ((ServerPlayer) player).connection.send(new ClientboundSetEntityLinkPacket((Mob) (Object) this, this.getLeashHolder()));
            cir.setReturnValue(InteractionResult.PASS);
        }
    }*/

    /*@Inject(method = "tickLeash", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;dropLeash(ZZ)V"))
    public void banner$unleash2(CallbackInfo ci) {
        Bukkit.getPluginManager().callEvent(new EntityUnleashEvent(this.getBukkitEntity(), this.isAlive() ?
                EntityUnleashEvent.UnleashReason.HOLDER_GONE : EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH));
    }

    @Inject(method = "dropLeash", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/Mob;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    public void banner$leashDropPost(boolean sendPacket, boolean dropLead, CallbackInfo ci) {
        this.banner$setForceDrops(false);
    }

    @Inject(method = "dropLeash", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    public void banner$leashDropPre(boolean sendPacket, boolean dropLead, CallbackInfo ci) {
        this.banner$setForceDrops(true);
    }

    @Inject(method = "restoreLeashFromSave", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/Mob;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private void banner$leashRestorePost(CallbackInfo ci) {
        this.banner$setForceDrops(false);
    }

    @Inject(method = "restoreLeashFromSave", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private void banner$leashRestorePre(CallbackInfo ci) {
        this.banner$setForceDrops(true);
    }*/

    @Inject(method = "startRiding", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;dropLeash(ZZ)V"))
    private void banner$unleashRide(Entity entityIn, boolean force, CallbackInfoReturnable<Boolean> cir) {
        Bukkit.getPluginManager().callEvent(new EntityUnleashEvent(this.getBukkitEntity(), EntityUnleashEvent.UnleashReason.UNKNOWN));
    }

    /*
    @Inject(method = "removeAfterChangingDimensions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;dropLeash(ZZ)V"))
    private void banner$unleashDead(CallbackInfo ci) {
        Bukkit.getPluginManager().callEvent(new EntityUnleashEvent(this.getBukkitEntity(), EntityUnleashEvent.UnleashReason.UNKNOWN));
    }*/

    /*
    @Eject(method = "convertTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$copySpawn(net.minecraft.world.level.Level world, Entity entityIn, CallbackInfoReturnable<Mob> cir) {
        EntityTransformEvent.TransformReason transformReason = banner$transform == null ? EntityTransformEvent.TransformReason.UNKNOWN : banner$transform;
        banner$transform = null;
        if (CraftEventFactory.callEntityTransformEvent((Mob) (Object) this, (LivingEntity) entityIn, transformReason).isCancelled()) {
            cir.setReturnValue(null);
            return false;
        } else {
            return world.addFreshEntity(entityIn);
        }
    }*/

    @Inject(method = "convertTo", at = @At("RETURN"))
    private <T extends Mob> void banner$cleanReason(EntityType<T> p_233656_1_, boolean p_233656_2_, CallbackInfoReturnable<T> cir) {
        this.level().pushAddEntityReason(null);
        this.banner$transform = null;
    }

    /*
    @Redirect(method = "doHurtTarget", at = @At(value = "INVOKE", target = "s"))
    public void banner$attackCombust(Entity entity, int seconds) {
        EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(this.getBukkitEntity(), entity.getBukkitEntity(), seconds);
        org.bukkit.Bukkit.getPluginManager().callEvent(combustEvent);
        if (!combustEvent.isCancelled()) {
            entity.banner$setSecondsOnFire(combustEvent.getDuration(), false);
        }
    }*/

    @Mixin(Mob.class)
    public abstract static class PaperSpawnAffect extends LivingEntity {

        protected PaperSpawnAffect(EntityType<? extends LivingEntity> entityType, Level level) {
            super(entityType, level);
        }
    }

    @Override
    public <T extends Mob> T convertTo(EntityType<T> entitytypes, boolean flag, EntityTransformEvent.TransformReason transformReason, CreatureSpawnEvent.SpawnReason spawnReason) {
        this.level().pushAddEntityReason(spawnReason);
        bridge$pushTransformReason(transformReason);
        return this.convertTo(entitytypes, flag);
    }

    @Override
    public void bridge$pushTransformReason(EntityTransformEvent.TransformReason transformReason) {
        this.banner$transform = transformReason;
    }

    @Override
    public boolean setTarget(LivingEntity entityliving, EntityTargetEvent.TargetReason reason, boolean fireEvent) {
        bridge$pushGoalTargetReason(reason, fireEvent);
        setTarget(entityliving);
        return banner$targetSuccess;
    }

    @Override
    public void bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason reason, boolean fireEvent) {
        if (fireEvent) {
            this.banner$reason = reason;
        } else {
            this.banner$reason = null;
        }
        banner$fireEvent = fireEvent;
    }

    @Override
    public SoundEvent getAmbientSound0() {
        return getAmbientSound();
    }

    @Override
    public void setPersistenceRequired(boolean persistenceRequired) {
        this.persistenceRequired = persistenceRequired;
    }

    @Override
    public boolean bridge$aware() {
        return aware;
    }

    @Override
    public void banner$setAware(boolean aware) {
        this.aware = aware;
    }

    @Override
    public boolean getBanner$targetSuccess() {
        return banner$targetSuccess;
    }
}