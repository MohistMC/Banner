package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PoweredRailBlock.class)
public class MixinPoweredRailBlock {

    @Inject(method = "updateState", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    public void banner$blockRedstone(BlockState state, Level worldIn, BlockPos pos, Block blockIn, CallbackInfo ci, boolean flag) {
        int power = flag ? 15 : 0;
        int newPower = CraftEventFactory.callRedstoneChange(worldIn, pos, power, 15 - power).getNewCurrent();
        if (newPower == power) {
            ci.cancel();
        }
    }
}
