package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.event.block.BlockFromToEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DragonEggBlock.class)
public class MixinDragonEggBlock {

    @Inject(method = "teleport",
            at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/level/Level;isClientSide:Z"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$tpEvent(BlockState state, Level level, BlockPos pos, CallbackInfo ci,
                                WorldBorder worldBorder, int i, BlockPos blockPos) {
        // CraftBukkit start
        org.bukkit.block.Block from = level.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        org.bukkit.block.Block to = level.getWorld().getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        BlockFromToEvent event = new BlockFromToEvent(from, to);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        blockPos = new BlockPos(event.getToBlock().getX(), event.getToBlock().getY(), event.getToBlock().getZ());
        // CraftBukkit end
    }

}
