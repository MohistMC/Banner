package com.mohistmc.banner.mixin.world.level.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SpongeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.util.BlockStateListPopulator;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SpongeBlock.class)
public abstract class MixinSpongeBlock extends Block {

    @Shadow @Final private static Direction[] ALL_DIRECTIONS;

    public MixinSpongeBlock(Properties properties) {
        super(properties);
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    private boolean removeWaterBreadthFirstSearch(Level world, BlockPos blockposition) {
        BlockStateListPopulator blockList = new BlockStateListPopulator(world); // CraftBukkit - Use BlockStateListPopulator
        BlockPos.breadthFirstTraversal(blockposition, 6, 65, (blockposition1, consumer) -> {
            Direction[] aenumdirection = ALL_DIRECTIONS;
            int i = aenumdirection.length;

            for (Direction enumdirection : aenumdirection) {
                consumer.accept(blockposition1.relative(enumdirection));
            }

        }, (blockposition1) -> {
            if (blockposition1.equals(blockposition)) {
                return true;
            } else {
                // CraftBukkit start
                BlockState iblockdata = blockList.getBlockState(blockposition1);
                FluidState fluid = blockList.getFluidState(blockposition1);
                // CraftBukkit end

                if (!fluid.is(FluidTags.WATER)) {
                    return false;
                } else {
                    Block block = iblockdata.getBlock();

                    if (block instanceof BucketPickup ifluidsource) {

                        if (!ifluidsource.pickupBlock(null, blockList, blockposition1, iblockdata).isEmpty()) { // CraftBukkit
                            return true;
                        }
                    }

                    if (iblockdata.getBlock() instanceof LiquidBlock) {
                        blockList.setBlock(blockposition1, Blocks.AIR.defaultBlockState(), 3); // CraftBukkit
                    } else {
                        if (!iblockdata.is(Blocks.KELP) && !iblockdata.is(Blocks.KELP_PLANT) && !iblockdata.is(Blocks.SEAGRASS) && !iblockdata.is(Blocks.TALL_SEAGRASS)) {
                            return false;
                        }

                        // CraftBukkit start
                        // TileEntity tileentity = iblockdata.hasBlockEntity() ? world.getBlockEntity(blockposition1) : null;

                        // dropResources(iblockdata, world, blockposition1, tileentity);
                        blockList.setBlock(blockposition1, Blocks.AIR.defaultBlockState(), 3);
                        // CraftBukkit end
                    }

                    return true;
                }
            }
        });
        // CraftBukkit start
        List<CraftBlockState> blocks = blockList.getList(); // Is a clone
        if (!blocks.isEmpty()) {
            final org.bukkit.block.Block bblock = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());

            SpongeAbsorbEvent event = new SpongeAbsorbEvent(bblock, (List<org.bukkit.block.BlockState>) (List) blocks);
            world.getCraftServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return false;
            }

            for (CraftBlockState block : blocks) {
                BlockPos blockposition1 = block.getPosition();
                BlockState iblockdata = world.getBlockState(blockposition1);
                FluidState fluid = world.getFluidState(blockposition1);

                if (fluid.is(FluidTags.WATER)) {
                    if (iblockdata.getBlock() instanceof BucketPickup && !((BucketPickup) iblockdata.getBlock()).pickupBlock(null, blockList, blockposition1, iblockdata).isEmpty()) {
                        // NOP
                    } else if (iblockdata.getBlock() instanceof LiquidBlock) {
                        // NOP
                    } else if (iblockdata.is(Blocks.KELP) || iblockdata.is(Blocks.KELP_PLANT) || iblockdata.is(Blocks.SEAGRASS) || iblockdata.is(Blocks.TALL_SEAGRASS)) {
                        BlockEntity tileentity = iblockdata.hasBlockEntity() ? world.getBlockEntity(blockposition1) : null;

                        dropResources(iblockdata, world, blockposition1, tileentity);
                    }
                }
                world.setBlock(blockposition1, block.getHandle(), block.getFlag());
            }

            return true;
        }
        return false;
        // CraftBukkit end
    }
}
