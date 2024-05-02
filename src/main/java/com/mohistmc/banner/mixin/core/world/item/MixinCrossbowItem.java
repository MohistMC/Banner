package com.mohistmc.banner.mixin.core.world.item;

import com.mohistmc.banner.bukkit.DistValidate;
import io.izzel.arclight.mixin.Eject;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CrossbowItem.class)
public class MixinCrossbowItem {

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

    @Eject(method = "shootProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static boolean banner$addEntity(Level world, Entity entityIn, CallbackInfo ci, Level worldIn, LivingEntity shooter) {
        if (banner$capturedBoolean.get()) {
            if (!world.addFreshEntity(entityIn)) {
                if (shooter instanceof ServerPlayer) {
                    ((ServerPlayer) shooter).getBukkitEntity().updateInventory();
                }
                ci.cancel();
            }
        }
        return true;
    }
}
