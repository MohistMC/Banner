package com.mohistmc.banner.mixin.world.level.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.NeighborUpdater;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NeighborUpdater.class)
public interface MixinNeighborUpdater {

    @Inject(method = "executeUpdate",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;handleNeighborChanged(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;Z)V"),
            cancellable = true)
    private static void banner$redstoneEvent(Level level, BlockState state, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving, CallbackInfo ci) {
        // CraftBukkit start
        CraftWorld cworld = ((ServerLevel) level).getWorld();
        if (cworld != null) {
            BlockPhysicsEvent event = new BlockPhysicsEvent(CraftBlock.at(level, pos),
                    CraftBlockData.fromData(state), CraftBlock.at(level, fromPos));
            if (event.isCancelled()) {
                ci.cancel();
            }
            // CraftBukkit end
        }
    }
}
