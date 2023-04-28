package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BambooStalkBlock.class)
public abstract class MixinBambooStalkBlock extends Block {

    @Shadow @Final public static IntegerProperty AGE;

    @Shadow @Final public static EnumProperty<BambooLeaves> LEAVES;

    boolean shouldUpdateOthers = false; // CraftBukkit

    public MixinBambooStalkBlock(Properties properties) {
        super(properties);
    }

    @Redirect(method = "performBonemeal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private <T extends Comparable<T>> T banner$skipIfCancel(BlockState state, Property<T> property) {
        if (!state.is(Blocks.BAMBOO)) {
            return (T) Integer.valueOf(1);
        } else {
            return state.getValue(property);
        }
    }

    @Redirect(method = "growBamboo",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
                    ordinal = 0))
    private boolean banner$cancelSetBlock(Level instance, BlockPos pos, BlockState newState, int flags) {
        return false;
    }

    @Redirect(method = "growBamboo",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
                    ordinal = 1))
    private boolean banner$cancelSetBlock0(Level instance, BlockPos pos, BlockState newState, int flags) {
        shouldUpdateOthers = true;
        return false;
    }

    @Inject(method = "growBamboo",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
                    ordinal = 2))
    private void banner$callBambooEvent(BlockState state, Level level, BlockPos pos, RandomSource random, int age, CallbackInfo ci) {
        BlockPos banner$blockPos = pos.below(2);
        BlockState banner$blockState2 = level.getBlockState(banner$blockPos);
        BlockState banner$blockState = level.getBlockState(pos.below());
        int banner$j = (Integer)state.getValue(AGE) != 1 && !banner$blockState2.is(Blocks.BAMBOO) ? 0 : 1;
        int banner$k = (age < 11 || !(random.nextFloat() < 0.25F)) && age != 15 ? 0 : 1;
        BambooLeaves banner$blockpropertybamboosize = BambooLeaves.NONE;
        if (age >= 1) {
            if (banner$blockState.is(Blocks.BAMBOO) && banner$blockState.getValue(LEAVES) != BambooLeaves.NONE) {
                if (banner$blockState.is(Blocks.BAMBOO) && banner$blockState.getValue(LEAVES) != BambooLeaves.NONE) {
                    banner$blockpropertybamboosize = BambooLeaves.LARGE;
                    if (banner$blockState2.is(Blocks.BAMBOO)) {
                        level.setBlock(pos.below(), (BlockState) banner$blockState.setValue(LEAVES, BambooLeaves.SMALL), 3);
                        level.setBlock(banner$blockPos, (BlockState) banner$blockState2.setValue(LEAVES, BambooLeaves.NONE), 3);
                    }
                }
            } else {
                banner$blockpropertybamboosize = BambooLeaves.SMALL;
            }
            // CraftBukkit start
            if (org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory.handleBlockSpreadEvent(level, pos, pos.above(), this.defaultBlockState().setValue(BambooStalkBlock.AGE, banner$j).setValue(BambooStalkBlock.LEAVES, banner$blockpropertybamboosize).setValue(BambooStalkBlock.STAGE, banner$k), 3)) {
                if (shouldUpdateOthers) {
                    level.setBlock(pos.below(), (BlockState) banner$blockState.setValue(BambooStalkBlock.LEAVES, BambooLeaves.SMALL), 3);
                    level.setBlock(banner$blockPos, (BlockState) banner$blockState2.setValue(BambooStalkBlock.LEAVES, BambooLeaves.NONE), 3);
                }
            }
        }
    }
}
