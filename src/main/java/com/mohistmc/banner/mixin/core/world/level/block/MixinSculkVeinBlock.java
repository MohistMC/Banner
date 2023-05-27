package com.mohistmc.banner.mixin.core.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.SculkVeinBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(SculkVeinBlock.class)
public abstract class MixinSculkVeinBlock {

    @Shadow protected abstract boolean attemptPlaceSculk(SculkSpreader spreader, LevelAccessor level, BlockPos pos, RandomSource random);

    /**
     * @author wdog5
     * @reason functionally replaced
     */
    @Overwrite
    public int attemptUseCharge(SculkSpreader.ChargeCursor cursor, LevelAccessor level, BlockPos pos, RandomSource random, SculkSpreader spreader, boolean bl) {
        if (bl && this.attemptPlaceSculk(spreader, level, cursor.getPos(), random, pos)) {
            return cursor.getCharge() - 1;
        } else {
            return random.nextInt(spreader.chargeDecayRate()) == 0 ? Mth.floor((float)cursor.getCharge() * 0.5F) : cursor.getCharge();
        }
    }

    private AtomicReference<BlockPos> banner$pos = new AtomicReference<>();

    private boolean attemptPlaceSculk(SculkSpreader spreader, LevelAccessor level, BlockPos pos, RandomSource random, BlockPos sourceBlock) {
        banner$pos.set(sourceBlock);
        return attemptPlaceSculk(spreader, level, pos, random);
    }

    @Redirect(method = "attemptPlaceSculk", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean banner$cancelSetBlock(LevelAccessor instance, BlockPos pos, BlockState state, int i) {
        return false;
    }

    @Inject(method = "attemptPlaceSculk", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$setBlock(SculkSpreader spreader, LevelAccessor level, BlockPos pos,
                                 RandomSource random, CallbackInfoReturnable<Boolean> cir,
                                 BlockState blockState, TagKey<Block> tagKey, Iterator var7, Direction direction,
                                 BlockPos blockPos, BlockState blockState2, BlockState blockState3) {
        // CraftBukkit start - Call BlockSpreadEvent
        if (!CraftEventFactory.handleBlockSpreadEvent(level, banner$pos.get(), blockPos, blockState2, 3)) {
            cir.setReturnValue(false);
        }
        // CraftBukkit end
    }
}
