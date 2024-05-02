package com.mohistmc.banner.mixin.core.world.item;

import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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

    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
    public void banner$cancelBreak(ItemStack stack, int amount, LivingEntity entityIn, Consumer<LivingEntity> onBroken) {
    }

    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$cancelAddEntity(Level instance, Entity entity) {
        return false;
    }

    @Inject(method = "releaseUsing",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void banner$addEntity(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged, CallbackInfo ci, Player player, int i, int j, ThrownTrident thrownTrident) {
        // CraftBukkit start
        if (!level.addFreshEntity(thrownTrident)) {
            if (player instanceof ServerPlayer) {
                ((ServerPlayer) player).getBukkitEntity().updateInventory();
            }
            ci.cancel();
        }
        stack.hurtAndBreak(1, player, (entity) ->
                entity.broadcastBreakEvent(livingEntity.getUsedItemHand()));
        ((ThrownTrident) thrownTrident).pickupItemStack = stack.copy();// SPIGOT-4511 update since damage call moved
        // CraftBukkkit end
    }

    @Inject(method = "releaseUsing", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/stats/Stat;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$hurtAndBreak(ItemStack stack, Level level, LivingEntity livingEntity,
                                     int timeCharged, CallbackInfo ci,
                                     Player player, int i, int j) {
        // CraftBukkit start
        if (i >= 10) {
            if (j <= 0 || player.isInWaterOrRain()) {
                if (!level.isClientSide && j != 0) {
                    stack.hurtAndBreak(1, player, (entityhuman1) -> {
                        entityhuman1.broadcastBreakEvent(livingEntity.getUsedItemHand());
                    });
                    // CraftBukkkit end
                }
            }
        }
    }

    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/entity/player/Player;getYRot()F"))
    public void banner$riptide(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft, CallbackInfo ci) {
        PlayerRiptideEvent event = new PlayerRiptideEvent(((ServerPlayer) entityLiving).getBukkitEntity(), CraftItemStack.asCraftMirror(stack));
        Bukkit.getPluginManager().callEvent(event);
    }
}
