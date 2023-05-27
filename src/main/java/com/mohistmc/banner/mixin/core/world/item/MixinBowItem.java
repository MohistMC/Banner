package com.mohistmc.banner.mixin.core.world.item;

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
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(BowItem.class)
public abstract class MixinBowItem extends ProjectileWeaponItem {


    public MixinBowItem(Properties properties) {
        super(properties);
    }

    private AtomicReference<EntityShootBowEvent> banner$shootEvent = new AtomicReference<>();

    @Inject(method = "releaseUsing",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$bowEvent(ItemStack stack, Level level, LivingEntity livingEntity,
                                 int timeCharged, CallbackInfo ci, Player player, boolean bl,
                                 ItemStack itemStack, int i, float f, boolean bl2,
                                 ArrowItem arrowItem, AbstractArrow abstractArrow,
                                 int j, int k) {
        // CraftBukkit start
        EntityShootBowEvent event = CraftEventFactory.callEntityShootBowEvent(player, stack, itemStack,
                abstractArrow, player.getUsedItemHand(), f, !bl2);
        banner$shootEvent.set(event);
        if (event.isCancelled()) {
            event.getProjectile().remove();
            ci.cancel();
        }
        bl2 = !event.shouldConsumeItem();
        // CraftBukkit end
    }

    @Redirect(method = "releaseUsing",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$cancelAddEntity(Level instance, Entity entity) {
        return false;
    }

    @Inject(method = "releaseUsing", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
            shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$handleArrowAdding(ItemStack stack, Level level, LivingEntity livingEntity,
                                          int timeCharged, CallbackInfo ci, Player player, boolean bl,
                                          ItemStack itemStack, int i, float f, boolean bl2,
                                          ArrowItem arrowItem, AbstractArrow abstractArrow) {
        // CraftBukkit start
        if (banner$shootEvent.get().getProjectile() == abstractArrow.getBukkitEntity()) {
            if (!level.addFreshEntity(abstractArrow)) {
                if (player instanceof ServerPlayer) {
                    ((ServerPlayer) player).getBukkitEntity().updateInventory();
                }
                ci.cancel();
            }
        }
        // CraftBukkit end
    }

}
