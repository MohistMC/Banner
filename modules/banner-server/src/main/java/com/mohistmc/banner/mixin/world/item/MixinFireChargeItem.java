package com.mohistmc.banner.mixin.world.item;

import com.mohistmc.banner.bukkit.DistValidate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockIgniteEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FireChargeItem.class)
public class MixinFireChargeItem {

    @Inject(method = "useOn", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/FireChargeItem;playSound(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    public void banner$blockIgnite(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, Level world, BlockPos blockPos) {
        if (DistValidate.isValid(context) && CraftEventFactory.callBlockIgniteEvent(world, blockPos, BlockIgniteEvent.IgniteCause.FIREBALL, context.getPlayer()).isCancelled()) {
            if (!context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
