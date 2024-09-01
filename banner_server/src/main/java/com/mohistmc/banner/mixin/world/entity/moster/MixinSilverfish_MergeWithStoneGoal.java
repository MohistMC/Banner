package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net.minecraft.world.entity.monster.Silverfish$SilverfishMergeWithStoneGoal")
public abstract class MixinSilverfish_MergeWithStoneGoal extends RandomStrollGoal {

    public MixinSilverfish_MergeWithStoneGoal(PathfinderMob pathfinderMob, double d) {
        super(pathfinderMob, d);
    }

    @Inject(method = "start", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private void banner$entityChangeBlock(CallbackInfo ci, LevelAccessor world, BlockPos blockPos, BlockState blockState) {
        if (!CraftEventFactory.callEntityChangeBlockEvent(this.mob, blockPos, InfestedBlock.infestedStateByHost(blockState))) {
            ci.cancel();
        }
    }
}
