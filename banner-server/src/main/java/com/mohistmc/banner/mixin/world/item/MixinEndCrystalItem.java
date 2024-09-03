package com.mohistmc.banner.mixin.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(EndCrystalItem.class)
public class MixinEndCrystalItem {

    @Inject(method = "useOn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;setShowBottom(Z)V",
            shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$handlePlaceEvent(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir,
                                         Level level, BlockPos blockPos, BlockState blockState,
                                         BlockPos blockPos2, double d, double e, double f,
                                         List<Entity> list, EndCrystal endCrystal) {
        // CraftBukkit start
        if (CraftEventFactory.callEntityPlaceEvent(context, endCrystal).isCancelled()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
        // CraftBukkit end
    }

}
