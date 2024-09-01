package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FenceGateBlock.class)
public class MixinFenceGateBlock {

    @Shadow @Final public static BooleanProperty POWERED;

    @Redirect(method = "neighborChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;hasNeighborSignal(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean banner$blockRedstone(Level world, BlockPos pos, BlockState state) {
        boolean powered = world.hasNeighborSignal(pos);
        boolean oldPowered = state.getValue(POWERED);
        if (oldPowered != powered) {
            int newPower = powered ? 15 : 0;
            int oldPower = oldPowered ? 15 : 0;
            Block bukkitBlock = CraftBlock.at(world, pos);
            BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(bukkitBlock, oldPower, newPower);
            Bukkit.getPluginManager().callEvent(eventRedstone);
            return eventRedstone.getNewCurrent() > 0;
        }
        return powered;
    }
}
