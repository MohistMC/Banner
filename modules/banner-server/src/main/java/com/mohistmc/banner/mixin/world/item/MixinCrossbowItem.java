package com.mohistmc.banner.mixin.world.item;

import com.llamalad7.mixinextras.sugar.Cancellable;
import com.mohistmc.banner.bukkit.DistValidate;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CrossbowItem.class)
public class MixinCrossbowItem {

    @Unique
    private static AtomicBoolean banner$capturedBoolean = new AtomicBoolean(true);

    @Inject(method = "shootProjectile", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
    private static void banner$entityShoot(Level worldIn, LivingEntity shooter, InteractionHand handIn, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean isCreativeMode, float velocity, float inaccuracy, float projectileAngle, CallbackInfo ci,
                                             boolean flag, Projectile proj) {
        if (!DistValidate.isValid(worldIn)) {
            banner$capturedBoolean.set(true);
            return;
        }
        EntityShootBowEvent event = CraftEventFactory.callEntityShootBowEvent(shooter, crossbow, projectile, proj, shooter.getUsedItemHand(), soundPitch, true);
        if (event.isCancelled()) {
            event.getProjectile().remove();
            ci.cancel();
        }
        banner$capturedBoolean.set(event.getProjectile() == proj.getBukkitEntity());
    }

    @Redirect(method = "shootProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static boolean banner$addEntity(Level instance, Entity entity, Level worldIn, LivingEntity shooter, @Cancellable CallbackInfo ci) {
        if (banner$capturedBoolean.get()) {
            if (!instance.addFreshEntity(entity)) {
                if (shooter instanceof ServerPlayer) {
                    ((ServerPlayer) shooter).getBukkitEntity().updateInventory();
                }
                ci.cancel();
            }
        }
        return true;
    }
}
