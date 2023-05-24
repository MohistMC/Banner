package com.mohistmc.banner.mixin.world.item;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignItem.class)
public class MixinSignItem {

    private static BlockPos openSign = BukkitExtraConstants.openSign; // CraftBukkit

    @Redirect(method = "updateCustomBlockEntityTag", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;openTextEdit(Lnet/minecraft/world/level/block/entity/SignBlockEntity;)V"))
    private void banner$cancelOpen(Player instance, SignBlockEntity signBlockEntity) {}

    @Inject(method = "updateCustomBlockEntityTag",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;openTextEdit(Lnet/minecraft/world/level/block/entity/SignBlockEntity;)V",
            shift = At.Shift.AFTER))
    private void banner$setOpenSign(BlockPos pos, Level level, Player player,
                                    ItemStack stack, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        // CraftBukkit start - SPIGOT-4678
        BukkitExtraConstants.openSign = pos;
        // CraftBukkit end
    }
}

