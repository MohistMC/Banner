package com.mohistmc.banner.mixin.world.item;

import com.llamalad7.mixinextras.sugar.Cancellable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TridentItem.class)
public class MixinTridentItem {

    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtWithoutBreaking(ILnet/minecraft/world/entity/player/Player;)V"))
    public void banner$cancelBreak(ItemStack instance, int i, Player player) {
    }

    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void banner$cancelPlaySound(Level instance, Player player, Entity entity, SoundEvent soundEvent, SoundSource soundSource, float f, float g) {

    }

    @Redirect(method = "releaseUsing",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileFromRotation(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;FFF)Lnet/minecraft/world/entity/projectile/Projectile;")
    )
    public <T extends Projectile> T banner$addEntity(Projectile.ProjectileFactory<T> projectileFactory, ServerLevel serverLevel, ItemStack itemStack, LivingEntity livingEntity, float f, float g, float h, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        // CraftBukkit start
        var result = Projectile.spawnProjectileFromRotation(projectileFactory, serverLevel, itemStack, livingEntity, f, g, h);
        if (result.isRemoved()) {
            if (livingEntity instanceof ServerPlayer) {
                ((ServerPlayer) livingEntity).getBukkitEntity().updateInventory();
            }
            cir.setReturnValue(false);
        }
        itemStack.hurtAndBreak(1, livingEntity, LivingEntity.getSlotForHand(livingEntity.getUsedItemHand()));
        ((ThrownTrident) result).pickupItemStack = itemStack.copy();// SPIGOT-4511 update since damage call moved
        // CraftBukkkit end
        return result;
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
