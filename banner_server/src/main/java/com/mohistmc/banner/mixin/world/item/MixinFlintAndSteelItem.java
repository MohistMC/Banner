package com.mohistmc.banner.mixin.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockIgniteEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FlintAndSteelItem.class)
public class MixinFlintAndSteelItem {

    @Inject(method = "useOn", cancellable = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V")
            , locals = LocalCapture.CAPTURE_FAILHARD)
    public void banner$blockIgnite(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir,
                                   Player player, Level level, BlockPos blockPos, BlockState blockState) {
        // CraftBukkit start - Store the clicked block
        if (CraftEventFactory.callBlockIgniteEvent(level, blockPos, BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL, player).isCancelled()) {
            context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            cir.setReturnValue(InteractionResult.PASS);
        }
        // CraftBukkit end
    }
}
