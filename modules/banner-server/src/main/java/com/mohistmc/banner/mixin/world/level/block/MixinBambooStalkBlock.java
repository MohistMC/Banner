package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BambooStalkBlock.class)
public abstract class MixinBambooStalkBlock {

    @Shadow @Final public static IntegerProperty AGE;
    @Shadow @Final public static IntegerProperty STAGE;

    @Shadow @Final public static EnumProperty<BambooLeaves> LEAVES;

    @Redirect(method = "performBonemeal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private <T extends Comparable<T>> T banner$skipIfCancel(BlockState state, Property<T> property) {
        if (!state.is(Blocks.BAMBOO)) {
            return (T) Integer.valueOf(1);
        } else {
            return state.getValue(property);
        }
    }

    /**
     * @author Mgazul
     * @reason 2023/8/13
     */
    @Overwrite
    protected void growBamboo(BlockState state, Level level, BlockPos pos, RandomSource random, int age) {
        BlockState blockState = level.getBlockState(pos.below());
        BlockPos blockPos = pos.below(2);
        BlockState blockState2 = level.getBlockState(blockPos);
        BambooLeaves bambooLeaves = BambooLeaves.NONE;
        boolean shouldUpdateOthers = false; // CraftBukkit
        if (age >= 1) {
            if (blockState.is(Blocks.BAMBOO) && blockState.getValue(LEAVES) != BambooLeaves.NONE) {
                if (blockState.is(Blocks.BAMBOO) && blockState.getValue(LEAVES) != BambooLeaves.NONE) {
                    bambooLeaves = BambooLeaves.LARGE;
                    if (blockState2.is(Blocks.BAMBOO)) {
                        shouldUpdateOthers = true; // CraftBukkit
                    }
                }
            } else {
                bambooLeaves = BambooLeaves.SMALL;
            }
        }

        int i = state.getValue(AGE) != 1 && !blockState2.is(Blocks.BAMBOO) ? 0 : 1;
        int j = (age < 11 || !(random.nextFloat() < 0.25F)) && age != 15 ? 0 : 1;
        // CraftBukkit start
        if (CraftEventFactory.handleBlockSpreadEvent(level, pos, pos.above(), ((BambooStalkBlock) (Object) this).defaultBlockState().setValue(AGE, i).setValue(LEAVES, bambooLeaves).setValue(STAGE, j), 3)) {
            if (shouldUpdateOthers) {
                level.setBlock(pos.below(), blockState.setValue(LEAVES, BambooLeaves.SMALL), 3);
                level.setBlock(blockPos, blockState2.setValue(LEAVES, BambooLeaves.NONE), 3);
            }
        }
        // CraftBukkit end
    }
}
