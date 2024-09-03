package com.mohistmc.banner.mixin.world.level.block;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SculkBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(SculkBlock.class)
public class MixinSculkBlock {

    private AtomicReference<BlockState> banner$state = new AtomicReference<>();

    @Redirect(method = "attemptUseCharge", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean banner$cancelSetBlock(LevelAccessor instance, BlockPos pos, BlockState state, int i) {
        return false;
    }

    @Inject(method = "attemptUseCharge",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/LevelAccessor;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$getState(SculkSpreader.ChargeCursor cursor,
                                 LevelAccessor level, BlockPos pos,
                                 RandomSource random, SculkSpreader spreader,
                                 boolean bl, CallbackInfoReturnable<Integer> cir,
                                 int i, BlockPos blockPos, boolean bl2, int j,
                                 BlockPos blockPos2, BlockState blockState) {
        banner$state.set(blockState);
    }

    @WrapWithCondition(method = "attemptUseCharge",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/LevelAccessor;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private boolean banner$wrapPlaySound(LevelAccessor instance, Player player, BlockPos pos, SoundEvent soundEvent, SoundSource soundSource, float v1, float v2) {
        return CraftEventFactory.handleBlockSpreadEvent(instance, pos, pos.above(), banner$state.get(), 3);
    }
}
