package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DaylightDetectorBlock.class)
public class MixinDaylightDetectorBlock {

    @ModifyVariable(method = "updateSignalStrength", index = 3, name = "i",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private static int banner$blockRedstone(int i, BlockState blockState, Level world, BlockPos blockPos) {
        return CraftEventFactory.callRedstoneChange(world, blockPos, blockState.getValue(DaylightDetectorBlock.POWER), i).getNewCurrent();
    }
}
