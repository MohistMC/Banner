package com.mohistmc.banner.mixin.world.entity.player;

import com.mohistmc.banner.injection.world.entity.player.InjectionPlayer;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftVector;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.spigotmc.SpigotWorldConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

//TODO fix inject methods
@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity implements InjectionPlayer {

    @Shadow protected FoodData foodData;

    @Shadow protected PlayerEnderChestContainer enderChestInventory;

    @Shadow @Final private Abilities abilities;

    @Shadow public abstract void tick();

    @Shadow private long timeEntitySatOnShoulder;

    @Shadow public abstract CompoundTag getShoulderEntityLeft();

    @Shadow public abstract CompoundTag getShoulderEntityRight();

    @Shadow public abstract void setShoulderEntityRight(CompoundTag entityCompound);

    @Shadow public abstract void setShoulderEntityLeft(CompoundTag entityCompound);

    @Shadow @Final private Inventory inventory;

    @Shadow public abstract Either<Player.BedSleepingProblem, net.minecraft.util.Unit> startSleepInBed(BlockPos bedPos);

    @Shadow public abstract float getAttackStrengthScale(float adjustTicks);

    @Shadow public abstract void sweepAttack();

    @Shadow public abstract void crit(Entity entityHit);

    @Shadow public abstract void magicCrit(Entity entityHit);

    @Shadow public abstract void causeFoodExhaustion(float exhaustion);

    protected MixinPlayer(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    public boolean fauxSleeping;
    public int oldLevel = -1;
    protected AtomicReference<Boolean> banner$forceSleep = new AtomicReference<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(CallbackInfo ci) {
        oldLevel = -1;
        this.foodData.setEntityhuman((net.minecraft.world.entity.player.Player) (Object) this);
        this.enderChestInventory.setOwner(this.getBukkitEntity());
    }

    @Inject(method = "turtleHelmetTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private void banner$turtleHelmet(CallbackInfo ci) {
        pushEffectCause(EntityPotionEffectEvent.Cause.TURTLE_HELMET);
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"))
    private void banner$healByRegen(CallbackInfo ci) {
        pushHealReason(EntityRegainHealthEvent.RegainReason.REGEN);
    }

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "RETURN", ordinal = 1))
    private void banner$playerDropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem, CallbackInfoReturnable<ItemEntity> cir, double d0, ItemEntity itemEntity) {
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) this.getBukkitEntity();
        org.bukkit.entity.Item drop = ( org.bukkit.entity.Item) itemEntity.getBukkitEntity();

        PlayerDropItemEvent event = new PlayerDropItemEvent(player, drop);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            org.bukkit.inventory.ItemStack cur = player.getInventory().getItemInHand();
            if (traceItem && (cur == null || cur.getAmount() == 0)) {
                // The complete stack was dropped
                player.getInventory().setItemInHand(drop.getItemStack());
            } else if (traceItem && cur.isSimilar(drop.getItemStack()) && cur.getAmount() < cur.getMaxStackSize() && drop.getItemStack().getAmount() == 1) {
                // Only one item is dropped
                cur.setAmount(cur.getAmount() + 1);
                player.getInventory().setItemInHand(cur);
            } else {
                // Fallback
                player.getInventory().addItem(drop.getItemStack());
            }
            cir.setReturnValue(null);
        }
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else if (this.abilities.invulnerable && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        } else {
            this.noActionTime = 0;
            if (this.getHealth() <= 0.0F) {
                return false;
            } else {
                if (source.scalesWithDifficulty()) {
                    if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
                        // amount = 0.0F;
                        return false;
                    }

                    if (this.level.getDifficulty() == Difficulty.EASY) {
                        amount = Math.min(amount / 2.0F + 1.0F, amount);
                    }

                    if (this.level.getDifficulty() == Difficulty.HARD) {
                        amount = amount * 3.0F / 2.0F;
                    }
                }

                boolean damaged = super.hurt(source, amount);
                if (damaged) {
                    this.removeEntitiesOnShoulder();
                }
                return damaged;
                //return amount == 0.0F ? false : super.attackEntityFrom(source, amount);
            }
        }
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public boolean canHarmPlayer(final net.minecraft.world.entity.player.Player entityhuman) {
        Team team;
        if (entityhuman instanceof ServerPlayer thatPlayer) {
            team =  thatPlayer.getBukkitEntity().getScoreboard().getPlayerTeam((thatPlayer.getBukkitEntity()));
            if (team == null || team.allowFriendlyFire()) {
                return true;
            }
        } else {
            final OfflinePlayer thisPlayer = Bukkit.getOfflinePlayer(entityhuman.getScoreboardName());
            team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(thisPlayer);
            if (team == null || team.allowFriendlyFire()) {
                return true;
            }
        }
        if ((Object) this instanceof ServerPlayer) {
            return !team.hasPlayer(((ServerPlayer) (Object) this).getBukkitEntity());
        }
        return !team.hasPlayer(Bukkit.getOfflinePlayer(this.getScoreboardName()));
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    protected void removeEntitiesOnShoulder() {
        if (this.timeEntitySatOnShoulder + 20L < this.level.getGameTime()) {
            if (this.spawnEntityFromShoulder(this.getShoulderEntityLeft())) {
                this.setShoulderEntityLeft(new CompoundTag());
            }
            if (this.spawnEntityFromShoulder(this.getShoulderEntityRight())) {
                this.setShoulderEntityRight(new CompoundTag());
            }
        }
    }

    private EntityExhaustionEvent.ExhaustionReason banner$exhaustReason;

    @Redirect(method = "causeFoodExhaustion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V"))
    private void banner$exhaustEvent(FoodData foodData, float amount) {
        EntityExhaustionEvent.ExhaustionReason reason = banner$exhaustReason == null ? EntityExhaustionEvent.ExhaustionReason.UNKNOWN : banner$exhaustReason;
        banner$exhaustReason = null;
        EntityExhaustionEvent event = CraftEventFactory.callPlayerExhaustionEvent((net.minecraft.world.entity.player.Player) (Object) this, reason, amount);
        if (!event.isCancelled()) {
            this.foodData.addExhaustion(event.getExhaustion());
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack, boolean silent) {
        this.verifyEquippedItem(stack);
        if (slot == EquipmentSlot.MAINHAND) {
            this.equipEventAndSound(slot, this.inventory.items.set(this.inventory.selected, stack), stack, silent);
        } else if (slot == EquipmentSlot.OFFHAND) {
            this.equipEventAndSound(slot, this.inventory.offhand.set(0, stack), stack, silent);
        } else if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            this.equipEventAndSound(slot, this.inventory.armor.set(slot.getIndex(), stack), stack, silent);
        }
    }

    @Override
    public Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos blockposition, boolean force) {
        banner$forceSleep.set(force);
        try {
            return this.startSleepInBed(blockposition);
        } finally {
            this.banner$forceSleep.set(false);
        }
    }

    @Inject(method = "actuallyHurt", cancellable = true, at = @At("HEAD"))
    private void banner$damageEntityCustom(DamageSource damageSrc, float damageAmount, CallbackInfo ci) {
        damageEntity0(damageSrc, damageAmount);
        ci.cancel();
    }

    @Inject(method = "stopSleepInBed", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;sleepCounter:I"))
    private void banner$wakeup(boolean flag, boolean flag1, CallbackInfo ci) {
        BlockPos blockPos = this.getSleepingPos().orElse(null);
        if (this.getBukkitEntity() instanceof org.bukkit.entity.Player player) {
            org.bukkit.block.Block bed;
            if (blockPos != null) {
                bed = CraftBlock.at(this.level, blockPos);
            } else {
                bed =  this.level.getWorld().getBlockAt(player.getLocation());
            }
            PlayerBedLeaveEvent event = new PlayerBedLeaveEvent(player, bed, true);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    @ModifyArg(method = "jumpFromGround", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"))
    private float banner$exhaustInfo(float f) {
        SpigotWorldConfig config =  level.bridge$spigotConfig();
        if (config != null) {
            if (this.isSprinting()) {
                f = config.jumpSprintExhaustion;
                pushExhaustReason(EntityExhaustionEvent.ExhaustionReason.JUMP_SPRINT);
            } else {
                f = config.jumpWalkExhaustion;
                pushExhaustReason(EntityExhaustionEvent.ExhaustionReason.JUMP);
            }
        }
        return f;
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setSharedFlag(IZ)V"))
    private void banner$toggleGlide(net.minecraft.world.entity.player.Player playerEntity, int flag, boolean set) {
        if (playerEntity.getSharedFlag(flag) != set && !CraftEventFactory.callToggleGlideEvent((net.minecraft.world.entity.player.Player) (Object) this, set).isCancelled()) {
            playerEntity.setSharedFlag(flag, set);
        }
    }

    @Inject(method = "checkMovementStatistics", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"))
    private void banner$exhauseCause1(double p_36379_, double p_36380_, double p_36381_, CallbackInfo ci) {
        pushExhaustReason(EntityExhaustionEvent.ExhaustionReason.SWIM);
    }

    @Inject(method = "checkMovementStatistics", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"))
    private void banner$exhauseCause2(double p_36379_, double p_36380_, double p_36381_, CallbackInfo ci) {
        pushExhaustReason(EntityExhaustionEvent.ExhaustionReason.WALK_UNDERWATER);
    }

    @Inject(method = "checkMovementStatistics", at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"))
    private void banner$exhauseCause3(double p_36379_, double p_36380_, double p_36381_, CallbackInfo ci) {
        pushExhaustReason(EntityExhaustionEvent.ExhaustionReason.WALK_ON_WATER);
    }

    @Inject(method = "checkMovementStatistics", at = @At(value = "INVOKE", ordinal = 3, target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"))
    private void banner$exhauseCause4(double p_36379_, double p_36380_, double p_36381_, CallbackInfo ci) {
        pushExhaustReason(EntityExhaustionEvent.ExhaustionReason.SPRINT);
    }

    @Inject(method = "checkMovementStatistics", at = @At(value = "INVOKE", ordinal = 4, target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"))
    private void banner$exhauseCause5(double p_36379_, double p_36380_, double p_36381_, CallbackInfo ci) {
        pushExhaustReason(EntityExhaustionEvent.ExhaustionReason.CROUCH);
    }

    @Inject(method = "checkMovementStatistics", at = @At(value = "INVOKE", ordinal = 5, target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"))
    private void banner$exhauseCause6(double p_36379_, double p_36380_, double p_36381_, CallbackInfo ci) {
        pushExhaustReason(EntityExhaustionEvent.ExhaustionReason.WALK);
    }

    @Inject(method = "startFallFlying", cancellable = true, at = @At("HEAD"))
    private void banner$startGlidingEvent(CallbackInfo ci) {
        if (CraftEventFactory.callToggleGlideEvent((net.minecraft.world.entity.player.Player) (Object) this, true).isCancelled()) {
            this.setSharedFlag(7, true);
            this.setSharedFlag(7, false);
            ci.cancel();
        }
    }

    @Inject(method = "stopFallFlying", cancellable = true, at = @At("HEAD"))
    private void banner$stopGlidingEvent(CallbackInfo ci) {
        if (CraftEventFactory.callToggleGlideEvent((net.minecraft.world.entity.player.Player) (Object) this, false).isCancelled()) {
            ci.cancel();
        }
    }

    @Override
    public CraftHumanEntity getBukkitEntity() {
        return (CraftHumanEntity) super.getBukkitEntity();
    }


    @Override
    public boolean bridge$fauxSleeping() {
        return fauxSleeping;
    }

    @Override
    public void banner$setFauxSleeping(boolean fauxSleeping) {
        this.fauxSleeping = fauxSleeping;
    }

    @Override
    public int bridge$oldLevel() {
        return oldLevel;
    }

    @Override
    public void banner$setOldLevel(int oldLevel) {
        this.oldLevel = oldLevel;
    }
}
