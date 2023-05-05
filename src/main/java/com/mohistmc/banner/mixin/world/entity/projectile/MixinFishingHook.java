package com.mohistmc.banner.mixin.world.entity.projectile;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.storage.loot.LootContext;
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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Mixin(FishingHook.class)
public abstract class MixinFishingHook extends Projectile {


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

    public int minWaitTime = 100;
    public int maxWaitTime = 600;
    public boolean applyLure = true;

    public MixinFishingHook(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "checkCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
    private void banner$collide(FishingHook fishingHook, HitResult hitResult) {
        this.preOnHit(hitResult);
    }

    @Inject(method = "catchingFish", at = @At(value = "FIELD", shift = At.Shift.AFTER, ordinal = 0, target = "Lnet/minecraft/world/entity/projectile/FishingHook;timeUntilHooked:I"))
    private void banner$attemptFail(BlockPos blockPos, CallbackInfo ci) {
        PlayerFishEvent event = new PlayerFishEvent(((ServerPlayer) this.getPlayerOwner()).getBukkitEntity(), null, (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.FAILED_ATTEMPT);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Inject(method = "catchingFish", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void banner$fishBite(BlockPos blockPos, CallbackInfo ci) {
        PlayerFishEvent event = new PlayerFishEvent(((ServerPlayer) this.getPlayerOwner()).getBukkitEntity(), null, (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.BITE);
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

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public int retrieve(ItemStack stack) {
        Player player = this.getPlayerOwner();
        if (!this.level.isClientSide && player != null && !this.shouldStopFishing(player)) {
            int i = 0;
            if (this.hookedIn != null) {
                PlayerFishEvent fishEvent = new PlayerFishEvent(((ServerPlayer) player).getBukkitEntity(),  this.hookedIn.getBukkitEntity(), (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.CAUGHT_ENTITY);
                Bukkit.getPluginManager().callEvent(fishEvent);
                if (fishEvent.isCancelled()) {
                    return 0;
                }
                this.pullEntity(this.hookedIn);
                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)player, stack, ((FishingHook) (Object) this), Collections.emptyList());
                this.level.broadcastEntityEvent(this, (byte)31);
                i = this.hookedIn instanceof ItemEntity ? 3 : 5;
            } else if (this.nibble > 0) {
                LootContext.Builder builder = (new LootContext.Builder((ServerLevel)this.level)).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.TOOL, stack).withParameter(LootContextParams.THIS_ENTITY, this).withRandom(this.random).withLuck((float)this.luck + player.getLuck());
                LootTable lootTable = this.level.getServer().getLootTables().get(BuiltInLootTables.FISHING);
                List<ItemStack> list = lootTable.getRandomItems(builder.create(LootContextParamSets.FISHING));
                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)player, stack, ((FishingHook) (Object) this), list);
                Iterator var7 = list.iterator();

                while(var7.hasNext()) {
                    ItemStack itemStack = (ItemStack)var7.next();
                    ItemEntity itemEntity = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), itemStack);
                    double d = player.getX() - this.getX();
                    double e = player.getY() - this.getY();
                    double f = player.getZ() - this.getZ();
                    double g = 0.1;
                    itemEntity.setDeltaMovement(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
                    this.level.addFreshEntity(itemEntity);
                    player.level.addFreshEntity(new ExperienceOrb(player.level, player.getX(), player.getY() + 0.5, player.getZ() + 0.5, this.random.nextInt(6) + 1));
                    if (itemStack.is(ItemTags.FISHES)) {
                        player.awardStat(Stats.FISH_CAUGHT, 1);
                    }
                }

                i = 1;
            }

            if (this.onGround) {
                PlayerFishEvent playerFishEvent = new PlayerFishEvent(((ServerPlayer) player).getBukkitEntity(), null, (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.IN_GROUND);
                Bukkit.getPluginManager().callEvent(playerFishEvent);

                if (playerFishEvent.isCancelled()) {
                    return 0;
                }
                i = 2;
            }

            if (i == 0) {
                PlayerFishEvent playerFishEvent = new PlayerFishEvent(((ServerPlayer) player).getBukkitEntity(), null, (FishHook) this.getBukkitEntity(), PlayerFishEvent.State.REEL_IN);
                Bukkit.getPluginManager().callEvent(playerFishEvent);
                if (playerFishEvent.isCancelled()) {
                    return 0;
                }
            }

            this.discard();
            return i;
        } else {
            return 0;
        }
    }
}
