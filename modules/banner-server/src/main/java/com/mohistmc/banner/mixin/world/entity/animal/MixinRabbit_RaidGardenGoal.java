package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net.minecraft.world.entity.animal.Rabbit$RaidGardenGoal")
public class MixinRabbit_RaidGardenGoal {

    @Shadow @Final private Rabbit rabbit;

    @Inject(method = "tick", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private void banner$entityChangeBlock(CallbackInfo ci, Level world, BlockPos blockPos, BlockState blockState, Block block, int i) {
        if (i == 0) {
            if (!CraftEventFactory.callEntityChangeBlockEvent(this.rabbit, blockPos, Blocks.AIR.defaultBlockState())) {
                ci.cancel();
            }
        } else {
            if (!CraftEventFactory.callEntityChangeBlockEvent(
                    this.rabbit,
                    blockPos,
                    blockState.setValue(CarrotBlock.AGE, i - 1)
            )) {
                ci.cancel();
            }
        }
    }
}
