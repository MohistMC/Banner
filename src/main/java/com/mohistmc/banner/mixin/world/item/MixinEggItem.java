package com.mohistmc.banner.mixin.world.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EggItem.class)
public class MixinEggItem {

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$cancelAddEntity(Level instance, Entity entity) {
        return false;
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
            shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$handleEggEntity(Level level, Player player, InteractionHand usedHand,
                                        CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
                                        ItemStack itemStack, ThrownEgg thrownEgg) {
        // CraftBukkit start
        if (!level.addFreshEntity(thrownEgg)) {
            if (player instanceof ServerPlayer) {
                ((ServerPlayer) player).getBukkitEntity().updateInventory();
            }
            cir.setReturnValue(InteractionResultHolder.fail(itemStack));
        }
        // CraftBukkit end
    }
}
