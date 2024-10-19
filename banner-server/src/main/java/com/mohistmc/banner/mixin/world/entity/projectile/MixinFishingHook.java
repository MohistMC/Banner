package com.mohistmc.banner.mixin.world.entity.projectile;

import com.mohistmc.banner.injection.world.entity.InjectionFishingHook;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Local;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.FishHook;
import org.bukkit.event.player.PlayerFishEvent;
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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FishingHook.class)
public abstract class MixinFishingHook extends Projectile implements InjectionFishingHook {


    // @formatter:off
    @Shadow public Entity hookedIn;
    @Shadow private int nibble;
    @Shadow @Final private int luck;
    @Shadow public abstract Player getPlayerOwner();
    @Shadow private int timeUntilHooked;
    @Shadow private int timeUntilLured;
    @Shadow @Final private int lureSpeed;
    @Shadow public abstract void pullEntity(Entity p_150156_);
    // @formatter:on

    @Shadow protected abstract boolean shouldStopFishing(Player player);

    @Shadow private float fishAngle;
    @Unique
    public int minWaitTime = 100;
    @Unique
    public int maxWaitTime = 600;
    @Unique
    public boolean applyLure = true;
    @Unique
    public int minLureTime = 20;
    @Unique
    public int maxLureTime = 80;
    @Unique
    public float minLureAngle = 0.0F;
    @Unique
    public float maxLureAngle = 360.0F;
    @Unique
    public boolean rainInfluenced = true;
    @Unique
    public boolean skyInfluenced = true;

    public MixinFishingHook(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "checkCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
    private void banner$collide(FishingHook fishingHook, HitResult hitResult) {
        this.preOnHit(hitResult);
    }

    @Inject(method = "catchingFish", at = @At(value = "FIELD", shift = At.Shift.AFTER, ordinal = 0, target = "Lnet/minecraft/world/entity/projectile/FishingHook;timeUntilHooked:I"))
    private void banner$attemptFail(BlockPos blockPos, CallbackInfo ci) {
        PlayerFishEvent event = new PlayerFishEvent((org.bukkit.entity.Player) this.getPlayerOwner().getBukkitEntity(), null, (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.FAILED_ATTEMPT);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Inject(method = "catchingFish", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void banner$fishBite(BlockPos blockPos, CallbackInfo ci) {
        PlayerFishEvent event = new PlayerFishEvent((org.bukkit.entity.Player) this.getPlayerOwner().getBukkitEntity(), null, (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.BITE);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "catchingFish", at = @At("RETURN"))
    private void banner$modifyWaitingTime(BlockPos p_37146_, CallbackInfo ci) {
        if (this.nibble <= 0 && this.timeUntilHooked <= 0 && this.timeUntilLured <= 0) {
            this.timeUntilLured = Mth.nextInt(this.random, this.minWaitTime, this.maxWaitTime);
            this.timeUntilLured -= (this.applyLure) ? this.lureSpeed * 20 * 5 : 0;
        }
    }

    @Redirect(method = "catchingFish", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isRainingAt(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean addRainCheck(Level instance, BlockPos position) {
        return this.rainInfluenced && instance.isRainingAt(position);
    }

    @Redirect(method = "catchingFish", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;canSeeSky(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean addSkyCheck(Level instance, BlockPos pos) {
        return this.skyInfluenced  && instance.isRainingAt(pos);
    }

    @Redirect(method = "catchingFish", at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/util/Mth;nextFloat(Lnet/minecraft/util/RandomSource;FF)F"))
    private float banner$lureAngleParam(RandomSource random, float p_216269_, float p_216270_) {
        return Mth.nextFloat(random, this.minLureAngle, this.maxLureAngle);
    }

    @Redirect(method = "catchingFish", at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I"))
    private int bannert$lureTimeParam(RandomSource random, int p_216273_, int p_216274_) {
        return Mth.nextInt(random, this.minLureTime, this.maxLureTime);
    }


    @Inject(method = "retrieve", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;pullEntity(Lnet/minecraft/world/entity/Entity;)V"))
    private void banner$catchEntity(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        PlayerFishEvent fishEvent = new PlayerFishEvent(((ServerPlayer) this.getPlayerOwner()).getBukkitEntity(), this.hookedIn.getBukkitEntity(), (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.CAUGHT_ENTITY);
        Bukkit.getPluginManager().callEvent(fishEvent);
        if (fishEvent.isCancelled()) {
            cir.setReturnValue(0);
        }
    }

    @Decorate(method = "retrieve", inject = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;setDeltaMovement(DDD)V"))
    private void banner$catchFish(ItemStack stack, @Local(ordinal = -1) ItemEntity itementity, @Local(allocate = "expToDrop") int expToDrop) throws Throwable {
        PlayerFishEvent playerFishEvent = new PlayerFishEvent(((ServerPlayer) this.getPlayerOwner()).getBukkitEntity(), itementity.getBukkitEntity(), (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.CAUGHT_FISH);
        playerFishEvent.setExpToDrop(this.random.nextInt(6) + 1);
        Bukkit.getPluginManager().callEvent(playerFishEvent);

        if (playerFishEvent.isCancelled()) {
            DecorationOps.cancel().invoke(0);
            return;
        }
        expToDrop = playerFishEvent.getExpToDrop();
        DecorationOps.blackhole().invoke(expToDrop);
    }

    @Decorate(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            slice = @Slice(from = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;DDDI)Lnet/minecraft/world/entity/ExperienceOrb;")))
    private boolean banner$spawnExpOrb(Level instance, Entity entity, ItemStack stack, @Local(allocate = "expToDrop") int expToDrop) throws Throwable {
        if (entity instanceof ExperienceOrb orb) {
            if (expToDrop <= 0) {
                return false;
            }
            orb.value = expToDrop;
        }
        return (boolean) DecorationOps.callsite().invoke(instance, entity);
    }

    @Inject(method = "retrieve", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;onGround()Z"))
    private void banner$onGround(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        if (this.onGround()) {
            PlayerFishEvent playerFishEvent = new PlayerFishEvent(((ServerPlayer) this.getPlayerOwner()).getBukkitEntity(), null, (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.IN_GROUND);
            Bukkit.getPluginManager().callEvent(playerFishEvent);

            if (playerFishEvent.isCancelled()) {
                cir.setReturnValue(0);
            }
        }
    }

    @Inject(method = "retrieve", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;discard()V"))
    private void banner$reelIn(ItemStack itemStack, CallbackInfoReturnable<Integer> cir, @com.llamalad7.mixinextras.sugar.Local Player player, @com.llamalad7.mixinextras.sugar.Local int i) {
        if (i == 0) {
            PlayerFishEvent playerFishEvent = new PlayerFishEvent(((ServerPlayer) player).getBukkitEntity(), null, (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.REEL_IN);
            Bukkit.getPluginManager().callEvent(playerFishEvent);
            if (playerFishEvent.isCancelled()) {
                cir.setReturnValue(0);
                return;
            }
        }
    }

    @Override
    public int bridge$minWaitTime() {
        return minWaitTime;
    }

    @Override
    public void banner$setMinWaitTime(int minWaitTime) {
        this.minWaitTime = minWaitTime;
    }

    @Override
    public int bridge$maxWaitTime() {
        return minWaitTime;
    }

    @Override
    public void banner$setMaxWaitTime(int minWaitTime) {
        this.minWaitTime = minWaitTime;
    }

    @Override
    public boolean bridge$applyLure() {
        return applyLure;
    }

    @Override
    public void banner$setApplyLure(boolean applyLure) {
        this.applyLure = applyLure;
    }

    @Override
    public int bridge$minLureTime() {
        return minLureTime;
    }

    @Override
    public void banner$setMinLureTime(int minLureTime) {
        this.minLureTime = minLureTime;
    }

    @Override
    public int bridge$maxLureTime() {
        return maxLureTime;
    }

    @Override
    public void banner$setMaxLureTime(int maxLureTime) {
        this.maxLureTime = maxLureTime;
    }

    @Override
    public float bridge$minLureAngle() {
        return minLureAngle;
    }

    @Override
    public void banner$setMinLureAnglee(float minLureAngle) {
        this.minLureAngle = minLureAngle;
    }

    @Override
    public float bridge$maxLureAngle() {
        return maxLureAngle;
    }

    @Override
    public void banner$setMaxLureAnglee(float maxLureAngle) {
        this.maxLureAngle = maxLureAngle;
    }

    @Override
    public boolean bridge$rainInfluenced() {
        return rainInfluenced;
    }

    @Override
    public void banner$setRainInfluenced(boolean rainInfluenced) {
        this.rainInfluenced = rainInfluenced;
    }

    @Override
    public boolean bridge$skyInfluenced() {
        return skyInfluenced;
    }

    @Override
    public void banner$setSkyInfluenced(boolean skyInfluenced) {
        this.skyInfluenced = skyInfluenced;
    }
}
