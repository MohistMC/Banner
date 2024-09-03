package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;

@Mixin(VineBlock.class)
public abstract class MixinVineBlock extends Block {

    public MixinVineBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    public static BooleanProperty getPropertyForFace(Direction face) {
        return null;
    }

    @Shadow protected abstract boolean canSpread(BlockGetter blockReader, BlockPos pos);

    @Shadow
    public static boolean isAcceptableNeighbour(BlockGetter blockReader, BlockPos neighborPos, Direction attachedFace) {
        return false;
    }

    @Shadow @Final public static BooleanProperty UP;

    @Shadow protected abstract boolean canSupportAtFace(BlockGetter level, BlockPos pos, Direction direction);

    @Shadow protected abstract boolean hasHorizontalConnection(BlockState state);

    @Shadow protected abstract BlockState copyRandomFaces(BlockState blockState, BlockState blockState2, RandomSource random);

    /**
     * @author wdog5
     * @reason bukkit event
     */
    @Overwrite
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getGameRules().getBoolean(GameRules.RULE_DO_VINES_SPREAD)) {
            if (random.nextInt(4) == 0) {
                Direction direction = Direction.getRandom(random);
                BlockPos blockPos = pos.above();
                BlockPos blockPos2;
                BlockState blockState;
                Direction direction2;
                if (direction.getAxis().isHorizontal() && !(Boolean)state.getValue(getPropertyForFace(direction))) {
                    if (this.canSpread(level, pos)) {
                        blockPos2 = pos.relative(direction);
                        blockState = level.getBlockState(blockPos2);
                        if (blockState.isAir()) {
                            direction2 = direction.getClockWise();
                            Direction direction3 = direction.getCounterClockWise();
                            boolean bl = (Boolean)state.getValue(getPropertyForFace(direction2));
                            boolean bl2 = (Boolean)state.getValue(getPropertyForFace(direction3));
                            BlockPos blockPos3 = blockPos2.relative(direction2);
                            BlockPos blockPos4 = blockPos2.relative(direction3);
                            // CraftBukkit start - Call BlockSpreadEvent
                            BlockPos source = pos;
                            if (bl && isAcceptableNeighbour(level, blockPos3, direction2)) {
                                CraftEventFactory.handleBlockSpreadEvent(level, source, blockPos2, (BlockState) this.defaultBlockState().setValue(getPropertyForFace(direction2), true), 2);
                            } else if (bl2 && isAcceptableNeighbour(level, blockPos4, direction3)) {
                                CraftEventFactory.handleBlockSpreadEvent(level, source, blockPos2, (BlockState) this.defaultBlockState().setValue(getPropertyForFace(direction3), true), 2);
                            } else {
                                Direction direction4 = direction.getOpposite();
                                if (bl && level.isEmptyBlock(blockPos3) && isAcceptableNeighbour(level, pos.relative(direction2), direction4)) {
                                    CraftEventFactory.handleBlockSpreadEvent(level, source, blockPos3, (BlockState) this.defaultBlockState().setValue(getPropertyForFace(direction4), true), 2);
                                } else if (bl2 && level.isEmptyBlock(blockPos4) && isAcceptableNeighbour(level, pos.relative(direction3), direction4)) {
                                    CraftEventFactory.handleBlockSpreadEvent(level, source, blockPos4, (BlockState) this.defaultBlockState().setValue(getPropertyForFace(direction4), true), 2);
                                } else if ((double)random.nextFloat() < 0.05 && isAcceptableNeighbour(level, blockPos2.above(), Direction.UP)) {
                                    CraftEventFactory.handleBlockSpreadEvent(level, source, blockPos2, (BlockState) this.defaultBlockState().setValue(UP, true), 2);
                                }
                            }
                        } else if (isAcceptableNeighbour(level, blockPos2, direction)) {
                            CraftEventFactory.handleBlockGrowEvent(level, pos, (BlockState) state.setValue(getPropertyForFace(direction), true), 2); // CraftBukkit
                        }

                    }
                } else {
                    if (direction == Direction.UP && pos.getY() < level.getMaxBuildHeight() - 1) {
                        if (this.canSupportAtFace(level, pos, direction)) {
                            CraftEventFactory.handleBlockGrowEvent(level, pos, (BlockState) state.setValue(VineBlock.UP, true), 2); // CraftBukkit
                            return;
                        }

                        if (level.isEmptyBlock(blockPos)) {
                            if (!this.canSpread(level, pos)) {
                                return;
                            }

                            BlockState blockState2 = state;
                            Iterator var17 = Direction.Plane.HORIZONTAL.iterator();

                            while(true) {
                                do {
                                    if (!var17.hasNext()) {
                                        if (this.hasHorizontalConnection(blockState2)) {
                                            CraftEventFactory.handleBlockSpreadEvent(level, pos, pos, blockState2, 2); // CraftBukkit
                                        }

                                        return;
                                    }

                                    direction2 = (Direction)var17.next();
                                } while(!random.nextBoolean() && isAcceptableNeighbour(level, blockPos.relative(direction2), direction2));

                                blockState2 = (BlockState)blockState2.setValue(getPropertyForFace(direction2), false);
                            }
                        }
                    }

                    if (pos.getY() > level.getMinBuildHeight()) {
                        blockPos2 = pos.below();
                        blockState = level.getBlockState(blockPos2);
                        if (blockState.isAir() || blockState.is(this)) {
                            BlockState blockState3 = blockState.isAir() ? this.defaultBlockState() : blockState;
                            BlockState blockState4 = this.copyRandomFaces(state, blockState3, random);
                            if (blockState3 != blockState4 && this.hasHorizontalConnection(blockState4)) {
                                CraftEventFactory.handleBlockSpreadEvent(level, pos, blockPos2, blockState4, 2); // CraftBukkit
                            }
                        }
                    }

                }
            }
        }
    }

}
