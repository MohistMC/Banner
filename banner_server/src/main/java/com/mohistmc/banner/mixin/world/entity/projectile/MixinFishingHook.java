package com.mohistmc.banner.mixin.world.entity.projectile;

import com.mohistmc.banner.injection.world.entity.InjectionFishingHook;
import java.util.Collections;
import java.util.List;
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
import net.minecraft.world.entity.projectile.ProjectileDeflection;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    @Shadow protected abstract void pullEntity(Entity p_150156_);
    // @formatter:on

    @Shadow protected abstract boolean shouldStopFishing(Player player);

    @Shadow private float fishAngle;
    public int minWaitTime = 100;
    public int maxWaitTime = 600;
    public boolean applyLure = true;
    public int minLureTime = 20;
    public int maxLureTime = 80;
    public float minLureAngle = 0.0F;
    public float maxLureAngle = 360.0F;
    public boolean rainInfluenced = true;
    public boolean skyInfluenced = true;

    public MixinFishingHook(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "checkCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;hitTargetOrDeflectSelf(Lnet/minecraft/world/phys/HitResult;)Lnet/minecraft/world/entity/projectile/ProjectileDeflection;"))
    private ProjectileDeflection banner$collide(FishingHook fishingHook, HitResult hitResult) {
        return this.preHitTargetOrDeflectSelf(hitResult);
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


    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public int retrieve(ItemStack itemstack) {
        Player entityhuman = this.getPlayerOwner();

        if (!this.level().isClientSide && entityhuman != null && !this.shouldStopFishing(entityhuman)) {
            int i = 0;

            if (this.hookedIn != null) {
                // CraftBukkit start
                PlayerFishEvent playerFishEvent = new PlayerFishEvent((org.bukkit.entity.Player) entityhuman.getBukkitEntity(), this.hookedIn.getBukkitEntity(), (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.CAUGHT_ENTITY);
                this.level().getCraftServer().getPluginManager().callEvent(playerFishEvent);

                if (playerFishEvent.isCancelled()) {
                    return 0;
                }
                // CraftBukkit end
                this.pullEntity(this.hookedIn);
                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer) entityhuman, itemstack, ((FishingHook) (Object) this), Collections.emptyList());
                this.level().broadcastEntityEvent(this, (byte) 31);
                i = this.hookedIn instanceof ItemEntity ? 3 : 5;
            } else if (this.nibble > 0) {
                LootParams lootparams = (new LootParams.Builder((ServerLevel) this.level())).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.TOOL, itemstack).withParameter(LootContextParams.THIS_ENTITY, this).withLuck((float) this.luck + entityhuman.getLuck()).create(LootContextParamSets.FISHING);
                LootTable loottable = this.level().getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);
                List<ItemStack> list = loottable.getRandomItems(lootparams);

                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer) entityhuman, itemstack, ((FishingHook) (Object) this), list);

                for (ItemStack itemstack1 : list) {
                    ItemEntity entityitem = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), itemstack1);
                    // CraftBukkit start
                    PlayerFishEvent playerFishEvent = new PlayerFishEvent((org.bukkit.entity.Player) entityhuman.getBukkitEntity(), entityitem.getBukkitEntity(), (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.CAUGHT_FISH);
                    playerFishEvent.setExpToDrop(this.random.nextInt(6) + 1);
                    this.level().getCraftServer().getPluginManager().callEvent(playerFishEvent);

                    if (playerFishEvent.isCancelled()) {
                        return 0;
                    }
                    // CraftBukkit end
                    double d0 = entityhuman.getX() - this.getX();
                    double d1 = entityhuman.getY() - this.getY();
                    double d2 = entityhuman.getZ() - this.getZ();
                    double d3 = 0.1D;

                    entityitem.setDeltaMovement(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);
                    this.level().addFreshEntity(entityitem);
                    // CraftBukkit start - this.random.nextInt(6) + 1 -> playerFishEvent.getExpToDrop()
                    if (playerFishEvent.getExpToDrop() > 0) {
                        entityhuman.level().addFreshEntity(new ExperienceOrb(entityhuman.level(), entityhuman.getX(), entityhuman.getY() + 0.5D, entityhuman.getZ() + 0.5D, playerFishEvent.getExpToDrop()));
                    }
                    // CraftBukkit end
                    if (itemstack1.is(ItemTags.FISHES)) {
                        entityhuman.awardStat(Stats.FISH_CAUGHT, 1);
                    }
                }

                i = 1;
            }

            if (this.onGround()) {
                // CraftBukkit start
                PlayerFishEvent playerFishEvent = new PlayerFishEvent((org.bukkit.entity.Player) entityhuman.getBukkitEntity(), null, (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.IN_GROUND);
                this.level().getCraftServer().getPluginManager().callEvent(playerFishEvent);

                if (playerFishEvent.isCancelled()) {
                    return 0;
                }
                // CraftBukkit end
                i = 2;
            }
            // CraftBukkit start
            if (i == 0) {
                PlayerFishEvent playerFishEvent = new PlayerFishEvent((org.bukkit.entity.Player) entityhuman.getBukkitEntity(), null, (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.REEL_IN);
                this.level().getCraftServer().getPluginManager().callEvent(playerFishEvent);
                if (playerFishEvent.isCancelled()) {
                    return 0;
                }
            }
            // CraftBukkit end

            this.discard();
            return i;
        } else {
            return 0;
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
