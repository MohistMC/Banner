package com.mohistmc.banner.mixin.world.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EnderpearlItem.class)
public class MixinEnderpearlItem extends Item {

    public MixinEnderpearlItem(Properties properties) {
        super(properties);
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$cancelEntityAdd(Level instance, Entity entity) {
        return false;
    }

    @Inject(method = "use", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
            shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$handleAdding(Level level, Player player, InteractionHand usedHand,
                                     CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
                                     ItemStack itemStack, ThrownEnderpearl thrownEnderpearl) {
        // CraftBukkit start - change order
        if (!level.addFreshEntity(thrownEnderpearl)) {
            if (player instanceof ServerPlayer) {
                ((ServerPlayer) player).getBukkitEntity().updateInventory();
            }
            cir.setReturnValue(InteractionResultHolder.fail(itemStack));
        }
        // CraftBukkit end
    }
}
