package com.mohistmc.banner.mixin.world.entity.item;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {

    // @formatter:off
    @Shadow @Final private static EntityDataAccessor<ItemStack> DATA_ITEM;
    @Shadow public int pickupDelay;
    @Shadow public abstract ItemStack getItem();
    @Shadow public UUID target;
    // @formatter:on

    public MixinItemEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;)V", cancellable = true, at = @At("HEAD"))
    private static void banner$itemMerge(ItemEntity from, ItemStack stack1, ItemEntity to, ItemStack stack2, CallbackInfo ci) {
        if (CraftEventFactory.callItemMergeEvent(to, from).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "hurt", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;markHurt()V"))
    private void banner$damageNonLiving(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CraftEventFactory.handleNonLivingEntityDamageEvent((ItemEntity) (Object) this, source, amount)) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "mergeWithNeighbours", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
    private AABB banner$resetMerge(AABB instance, double x, double y, double z) {
        // Spigot start
        double radius = level().bridge$spigotConfig().itemMerge;
        return instance.inflate(radius, radius - 0.5D, radius);
        // Spigot end
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void playerTouch(final Player entity) {
        if (!this.level().isClientSide) {
            if (this.pickupDelay > 0) return;
            ItemStack itemstack = this.getItem();
            int i = itemstack.getCount();
            final int canHold =  entity.getInventory().canHold(itemstack);
            final int remaining = itemstack.getCount() - canHold;
            if (this.pickupDelay <= 0 && canHold > 0) {
                itemstack.setCount(canHold);
                final PlayerPickupItemEvent playerEvent = new PlayerPickupItemEvent(((ServerPlayer) entity).getBukkitEntity(), (Item) this.getBukkitEntity(), remaining);
                playerEvent.setCancelled(!playerEvent.getPlayer().getCanPickupItems());
                Bukkit.getPluginManager().callEvent(playerEvent);
                if (playerEvent.isCancelled()) {
                    itemstack.setCount(canHold + remaining);
                    return;
                }
                final EntityPickupItemEvent entityEvent = new EntityPickupItemEvent(((ServerPlayer) entity).getBukkitEntity(), (Item) this.getBukkitEntity(), remaining);
                entityEvent.setCancelled(!entityEvent.getEntity().getCanPickupItems());
                Bukkit.getPluginManager().callEvent(entityEvent);
                if (entityEvent.isCancelled()) {
                    itemstack.setCount(canHold + remaining);
                    return;
                }
                ItemStack current = this.getItem();
                if (!itemstack.equals(current)) {
                    itemstack = current;
                } else {
                    itemstack.setCount(canHold + remaining);
                }
                this.pickupDelay = 0;
            } else if (this.pickupDelay == 0) {
                this.pickupDelay = -1;
            }
            ItemStack copy = itemstack.copy();
            if (this.pickupDelay == 0 && (this.target == null || this.target.equals(entity.getUUID())) && entity.getInventory().add(itemstack)) {
                copy.setCount(copy.getCount() - itemstack.getCount());
                entity.take((ItemEntity) (Object) this, i);
                if (itemstack.isEmpty()) {
                    this.discard();
                    itemstack.setCount(i);
                }
                entity.awardStat(Stats.ITEM_PICKED_UP.get(itemstack.getItem()), i);
                entity.onItemPickup((ItemEntity) (Object) this);
            }
        }
    }

    @Inject(method = "setItem", at = @At("RETURN"))
    private void banner$markDirty(ItemStack stack, CallbackInfo ci) {
        this.getEntityData().markDirty(DATA_ITEM);
    }

    @Redirect(method = "merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;setItem(Lnet/minecraft/world/item/ItemStack;)V"))
    private static void banner$setNonEmpty(ItemEntity itemEntity, ItemStack stack) {
        if (!stack.isEmpty()) {
            itemEntity.setItem(stack);
        }
    }
}
