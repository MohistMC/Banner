package com.mohistmc.banner.mixin.core.world.item;

import com.mohistmc.banner.bukkit.DistValidate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BowItem.class)
public abstract class MixinBowItem extends ProjectileWeaponItem {

    @Unique
    private EntityShootBowEvent event;
    @Unique
    private Player banner$player;

    public MixinBowItem(Properties properties) {
        super(properties);
    }

    /*
    @Inject(method = "releaseUsing",
            at = @At(value = "INVOKE",
                    target = "hur"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$shootBowEvent(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged,
                                      CallbackInfo ci, Player player, boolean bl, ItemStack itemStack, int i,
                                      float f, boolean bl2, ArrowItem arrowItem, AbstractArrow abstractArrow, int j, int k) {
        banner$player = player;
        // CraftBukkit start
        event = CraftEventFactory.callEntityShootBowEvent(player, stack, itemStack,
                abstractArrow, player.getUsedItemHand(), f, !bl2);
        if (event.isCancelled()) {
            event.getProjectile().remove();
            ci.cancel();
        }
        // CraftBukkit end
    }

    @Inject(method = "releaseUsing",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
                    shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$shootBowEvent0(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged,
                                       CallbackInfo ci, Player player, boolean bl, ItemStack itemStack, int i,
                                       float f, boolean bl2, ArrowItem arrowItem, AbstractArrow abstractArrow, int j, int k) {
        bl2 = !event.shouldConsumeItem(); // Banner
    }

    @Redirect(method = "releaseUsing",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$checkAddEntity(Level level, Entity entity) {
        return false;
    }

    @Inject(method = "releaseUsing",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$checkAddEntity(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged,
                                       CallbackInfo ci, Player player, boolean bl, ItemStack itemStack, int i, float f,
                                       boolean bl2, ArrowItem arrowItem, AbstractArrow abstractArrow) {
        // CraftBukkit start
        if (event.getProjectile() == abstractArrow.getBukkitEntity()) {
            // Baner start - fix mixin
            level.addFreshEntity(abstractArrow);
            if (DistValidate.isValid(level)) {
                if (!((ServerLevel) level).canAddFreshEntity()) {
                    if (banner$player instanceof ServerPlayer) {
                        ((ServerPlayer) banner$player).getBukkitEntity().updateInventory();
                    }
                    ci.cancel();
                }
            }
            // Banner end
        }
        // CraftBukkit end
    }
    */
}