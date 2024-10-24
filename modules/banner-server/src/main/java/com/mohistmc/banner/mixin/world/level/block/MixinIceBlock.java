package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IceBlock.class)
public class MixinIceBlock {

    @Inject(method = "melt", cancellable = true, at = @At("HEAD"))
    public void banner$blockFade(BlockState blockState, Level world, BlockPos blockPos, CallbackInfo ci) {
        if (CraftEventFactory.callBlockFadeEvent(world, blockPos, world.dimensionType().ultraWarm()
                ? Blocks.AIR.defaultBlockState() : Blocks.WATER.defaultBlockState()).isCancelled()) {
            ci.cancel();
        }
    }
}
