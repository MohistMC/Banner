package com.mohistmc.banner.mixin.world.item;

import com.mohistmc.banner.asm.annotation.TransformAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignItem.class)
public class MixinSignItem {

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static BlockPos openSign; // CraftBukkit

    @Redirect(method = "updateCustomBlockEntityTag", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/SignBlock;openTextEdit(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/entity/SignBlockEntity;Z)V"))
    private void banner$cancelOpen(SignBlock instance, Player player, SignBlockEntity signBlockEntity, boolean bl) {}

    @Inject(method = "updateCustomBlockEntityTag",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/SignBlock;openTextEdit(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/entity/SignBlockEntity;Z)V",
            shift = At.Shift.AFTER))
    private void banner$setOpenSign(BlockPos pos, Level level, Player player,
                                    ItemStack stack, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        // CraftBukkit start - SPIGOT-4678
        openSign = pos;
        // CraftBukkit end
    }
}

