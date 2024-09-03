package com.mohistmc.banner.mixin.world.item;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TridentItem.class)
public class MixinTridentItem {

    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"))
    public void banner$cancelBreak(ItemStack instance, int i, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
    }

    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$cancelAddEntity(Level instance, Entity entity) {
        return false;
    }

    @Inject(method = "releaseUsing",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void banner$addEntity(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo ci, Player player, int j, float f, Holder holder, ThrownTrident thrownTrident) {
        // CraftBukkit start
        if (!level.addFreshEntity(thrownTrident)) {
            if (player instanceof ServerPlayer) {
                ((ServerPlayer) player).getBukkitEntity().updateInventory();
            }
            ci.cancel();
        }
        itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(livingEntity.getUsedItemHand()));
        ((ThrownTrident) thrownTrident).pickupItemStack = itemStack.copy();// SPIGOT-4511 update since damage call moved
        // CraftBukkkit end
    }

    // Banner TODO fixme
    /*
    @Inject(method = "releaseUsing", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/stats/Stat;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$hurtAndBreak(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo ci, Player player) {
        // CraftBukkit start
        if (i >= 10) {
            if (f <= 0 || player.isInWaterOrRain()) {
                if (!level.isClientSide && f != 0) {
                    itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(livingEntity.getUsedItemHand()));
                    // CraftBukkkit end
                }
            }
        }
    }*/

    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/entity/player/Player;getYRot()F"))
    public void banner$riptide(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft, CallbackInfo ci) {
        PlayerRiptideEvent event = new PlayerRiptideEvent(((ServerPlayer) entityLiving).getBukkitEntity(), CraftItemStack.asCraftMirror(stack));
        Bukkit.getPluginManager().callEvent(event);
    }
}
