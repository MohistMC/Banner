package com.mohistmc.banner.mixin.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EnderEyeItem.class)
public class MixinEnderEyeItem {

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$cancelEntityAdd(Level instance, Entity entity) {
        return false;
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
            shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$handleAddEntity(Level level, Player player, InteractionHand usedHand,
                                        CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
                                        ItemStack itemStack, BlockHitResult blockHitResult, ServerLevel serverLevel,
                                        BlockPos blockPos, EyeOfEnder eyeOfEnder) {
        // CraftBukkit start
        if (!level.addFreshEntity(eyeOfEnder)) {
            cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.FAIL, itemStack));
        }
        // CraftBukkit end
    }
}
