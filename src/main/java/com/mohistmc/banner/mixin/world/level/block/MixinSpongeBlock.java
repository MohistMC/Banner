package com.mohistmc.banner.mixin.world.level.block;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_19_R3.util.BlockStateListPopulator;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;
import java.util.Queue;

@Mixin(SpongeBlock.class)
public abstract class MixinSpongeBlock extends Block {

    public MixinSpongeBlock(Properties properties) {
        super(properties);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    private boolean removeWaterBreadthFirstSearch(Level level, BlockPos pos) {
        Queue<Tuple<BlockPos, Integer>> queue = Lists.newLinkedList();
        queue.add(new Tuple(pos, 0));
        int i = 0;
        BlockStateListPopulator blockList = new BlockStateListPopulator(level); // CraftBukkit - Use BlockStateListPopulator

        while(!queue.isEmpty()) {
            Tuple<BlockPos, Integer> tuple = (Tuple)queue.poll();
            BlockPos blockPos = (BlockPos)tuple.getA();
            int j = (Integer)tuple.getB();
            Direction[] var8 = Direction.values();
            int var9 = var8.length;

            for (Direction direction : var8) {
                BlockPos blockPos2 = blockPos.relative(direction);
                BlockState blockState = blockList.getBlockState(blockPos2);
                FluidState fluidState = blockList.getFluidState(blockPos2);
                if (fluidState.is(FluidTags.WATER)) {
                    if (blockState.getBlock() instanceof BucketPickup && !((BucketPickup) blockState.getBlock()).pickupBlock(blockList, blockPos2, blockState).isEmpty()) {
                        ++i;
                        if (j < 6) {
                            queue.add(new Tuple(blockPos2, j + 1));
                        }
                    } else if (blockState.getBlock() instanceof LiquidBlock) {
                        blockList.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 3);// CraftBukkit
                        ++i;
                        if (j < 6) {
                            queue.add(new Tuple(blockPos2, j + 1));
                        }
                    } else if (!blockState.is(Blocks.KELP) && !blockState.is(Blocks.KELP_PLANT) && !blockState.is(Blocks.SEAGRASS) && !blockState.is(Blocks.TALL_SEAGRASS)) {
                        // CraftBukkit start
                        // BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(blockPos2) : null;
                        // dropResources(blockState, level, blockPos2, blockEntity);
                        blockList.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 3);
                        // CraftBukkit end
                        ++i;
                        if (j < 6) {
                            queue.add(new Tuple(blockPos2, j + 1));
                        }
                    }
                }
            }

            if (i > 64) {
                break;
            }
        }

        // CraftBukkit start
        List<CraftBlockState> blocks = blockList.getList(); // Is a clone
        if (!blocks.isEmpty()) {
            final org.bukkit.block.Block bblock = level.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
            SpongeAbsorbEvent event = new SpongeAbsorbEvent(bblock, (List<org.bukkit.block.BlockState>) (List) blocks);
            level.getCraftServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return false;
            }

            for (CraftBlockState block : blocks) {
                BlockPos blockposition2 = block.getPosition();
                BlockState iblockdata = level.getBlockState(blockposition2);
                FluidState fluid = level.getFluidState(blockposition2);
                if (fluid.is(Fluids.WATER)) {
                    if (iblockdata.getBlock() instanceof BucketPickup && !((BucketPickup) iblockdata.getBlock()).pickupBlock(blockList, blockposition2, iblockdata).isEmpty()) {
                        // NOP
                    } else if (iblockdata.getBlock() instanceof LiquidBlock) {
                        // NOP
                    } else if (!iblockdata.is(Blocks.KELP) && !iblockdata.is(Blocks.KELP_PLANT) && !iblockdata.is(Blocks.SEAGRASS) && !iblockdata.is(Blocks.TALL_SEAGRASS)) {
                        BlockEntity tileentity = iblockdata.hasBlockEntity() ? level.getBlockEntity(blockposition2) : null;
                        dropResources(iblockdata, level, blockposition2, tileentity);
                    }
                }
                level.setBlock(blockposition2, block.getHandle(), block.getFlag());
            }
        }
        // CraftBukkit end

        return i > 0;
    }
}
