package com.mohistmc.banner.mixin.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(PistonBaseBlock.class)
public class MixinPistonBaseBlock extends DirectionalBlock {

    // @formatter:off
    @Shadow @Final private boolean isSticky;

    protected MixinPistonBaseBlock(Properties properties) {
        super(properties);
    }
    // @formatter:on

    @Inject(method = "checkIfExtend", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;blockEvent(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;II)V"))
    public void banner$pistonRetract(Level worldIn, BlockPos pos, BlockState state, CallbackInfo ci, Direction direction) {
        if (!this.isSticky) {
            Block block = CraftBlock.at(worldIn, pos);
            BlockPistonRetractEvent event = new BlockPistonRetractEvent(block, ImmutableList.of(), CraftBlock.notchToBlockFace(direction));
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    /**
     * @author wdog6
     * @reason bukkit event
     */
    @Overwrite
    private boolean moveBlocks(Level level, BlockPos pos, Direction direction, boolean extending) {
        BlockPos blockPos = pos.relative(direction);
        if (!extending && level.getBlockState(blockPos).is(Blocks.PISTON_HEAD)) {
            level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 20);
        }

        PistonStructureResolver pistonStructureResolver = new PistonStructureResolver(level, pos, direction, extending);
        if (!pistonStructureResolver.resolve()) {
            return false;
        } else {
            Map<BlockPos, BlockState> map = Maps.newHashMap();
            List<BlockPos> list = pistonStructureResolver.getToPush();
            List<BlockState> list2 = Lists.newArrayList();

            for(int i = 0; i < list.size(); ++i) {
                BlockPos blockPos2 = (BlockPos)list.get(i);
                BlockState blockState = level.getBlockState(blockPos2);
                list2.add(blockState);
                map.put(blockPos2, blockState);
            }

            List<BlockPos> list3 = pistonStructureResolver.getToDestroy();
            final Block craftBlock = CraftBlock.at(level, pos);

            final List<BlockPos> moved = pistonStructureResolver.getToPush();
            final List<BlockPos> broken = pistonStructureResolver.getToDestroy();

            class BlockList extends AbstractList<Block> {

                @Override
                public int size() {
                    return moved.size() + broken.size();
                }

                @Override
                public org.bukkit.block.Block get(int index) {
                    if (index >= size() || index < 0) {
                        throw new ArrayIndexOutOfBoundsException(index);
                    }
                    BlockPos pos = index < moved.size() ? moved.get(index) : broken.get(index - moved.size());
                    return craftBlock.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
                }
            }

            List<Block> blocks = new BlockList();

            Direction banner$direction = extending ? pistonStructureResolver.getPushDirection() : pistonStructureResolver.getPushDirection().getOpposite();
            BlockPistonEvent event;
            if (extending) {
                event = new BlockPistonExtendEvent(craftBlock, blocks, CraftBlock.notchToBlockFace(banner$direction));
            } else {
                event = new BlockPistonRetractEvent(craftBlock, blocks, CraftBlock.notchToBlockFace(banner$direction));
            }
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                for (BlockPos b : broken) {
                    level.sendBlockUpdated(b, Blocks.AIR.defaultBlockState(), level.getBlockState(b), 3);
                }
                for (BlockPos b : moved) {
                    level.sendBlockUpdated(b, Blocks.AIR.defaultBlockState(), level.getBlockState(b), 3);
                    b = b.relative(banner$direction);
                    level.sendBlockUpdated(b, Blocks.AIR.defaultBlockState(), level.getBlockState(b), 3);
                }
                return false;
            }
            BlockState[] blockStates = new BlockState[list.size() + list3.size()];
            Direction direction2 = extending ? direction : direction.getOpposite();
            int j = 0;

            int k;
            BlockPos blockPos3;
            BlockState blockState2;
            for(k = list3.size() - 1; k >= 0; --k) {
                blockPos3 = (BlockPos)list3.get(k);
                blockState2 = level.getBlockState(blockPos3);
                BlockEntity blockEntity = blockState2.hasBlockEntity() ? level.getBlockEntity(blockPos3) : null;
                dropResources(blockState2, level, blockPos3, blockEntity);
                level.setBlock(blockPos3, Blocks.AIR.defaultBlockState(), 18);
                level.gameEvent(GameEvent.BLOCK_DESTROY, blockPos3, GameEvent.Context.of(blockState2));
                if (!blockState2.is(BlockTags.FIRE)) {
                    level.addDestroyBlockEffect(blockPos3, blockState2);
                }

                blockStates[j++] = blockState2;
            }

            for(k = list.size() - 1; k >= 0; --k) {
                blockPos3 = (BlockPos)list.get(k);
                blockState2 = level.getBlockState(blockPos3);
                blockPos3 = blockPos3.relative(direction2);
                map.remove(blockPos3);
                BlockState blockState3 = (BlockState)Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, direction);
                level.setBlock(blockPos3, blockState3, 68);
                level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockPos3, blockState3, (BlockState)list2.get(k), direction, extending, false));
                blockStates[j++] = blockState2;
            }

            if (extending) {
                PistonType pistonType = this.isSticky ? PistonType.STICKY : PistonType.DEFAULT;
                BlockState blockState4 = (BlockState)((BlockState)Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, direction)).setValue(PistonHeadBlock.TYPE, pistonType);
                blockState2 = (BlockState)((BlockState)Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction)).setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
                map.remove(blockPos);
                level.setBlock(blockPos, blockState2, 68);
                level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockPos, blockState2, blockState4, direction, true, true));
            }

            BlockState blockState5 = Blocks.AIR.defaultBlockState();
            Iterator var25 = map.keySet().iterator();

            while(var25.hasNext()) {
                BlockPos blockPos4 = (BlockPos)var25.next();
                level.setBlock(blockPos4, blockState5, 82);
            }

            var25 = map.entrySet().iterator();

            BlockPos blockPos5;
            while(var25.hasNext()) {
                Map.Entry<BlockPos, BlockState> entry = (Map.Entry)var25.next();
                blockPos5 = (BlockPos)entry.getKey();
                BlockState blockState6 = (BlockState)entry.getValue();
                blockState6.updateIndirectNeighbourShapes(level, blockPos5, 2);
                blockState5.updateNeighbourShapes(level, blockPos5, 2);
                blockState5.updateIndirectNeighbourShapes(level, blockPos5, 2);
            }

            j = 0;

            int l;
            for(l = list3.size() - 1; l >= 0; --l) {
                blockState2 = blockStates[j++];
                blockPos5 = (BlockPos)list3.get(l);
                blockState2.updateIndirectNeighbourShapes(level, blockPos5, 2);
                level.updateNeighborsAt(blockPos5, blockState2.getBlock());
            }

            for(l = list.size() - 1; l >= 0; --l) {
                level.updateNeighborsAt((BlockPos)list.get(l), blockStates[j++].getBlock());
            }

            if (extending) {
                level.updateNeighborsAt(blockPos, Blocks.PISTON_HEAD);
            }

            return true;
        }
    }
}
