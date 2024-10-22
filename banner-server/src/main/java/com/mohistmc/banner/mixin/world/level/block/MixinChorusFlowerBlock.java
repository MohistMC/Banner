package com.mohistmc.banner.mixin.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChorusFlowerBlock.class)
public abstract class MixinChorusFlowerBlock extends Block {


    // @formatter:off
    @Shadow @Final public static IntegerProperty AGE;

    public MixinChorusFlowerBlock(Properties properties) {
        super(properties);
    }

    @Shadow private static boolean allNeighborsEmpty(LevelReader worldIn, BlockPos pos, @Nullable Direction excludingSide) { return false; }
    @Shadow protected abstract void placeGrownFlower(Level worldIn, BlockPos pos, int age);
    @Shadow protected abstract void placeDeadFlower(Level worldIn, BlockPos pos);
    // @formatter:on

    @Shadow @Final private Block plant;

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
        BlockPos blockpos = pos.above();
        if (worldIn.isEmptyBlock(blockpos) && blockpos.getY() < 256) {
            int i = state.getValue(AGE);
            if (i < 5) {
                boolean flag = false;
                boolean flag1 = false;
                BlockState blockstate = worldIn.getBlockState(pos.below());
                Block block = blockstate.getBlock();
                if (block == Blocks.END_STONE) {
                    flag = true;
                } else if (block == this.plant) {
                    int j = 1;

                    for (int k = 0; k < 4; ++k) {
                        Block block1 = worldIn.getBlockState(pos.below(j + 1)).getBlock();
                        if (block1 != this.plant) {
                            if (block1 == Blocks.END_STONE) {
                                flag1 = true;
                            }
                            break;
                        }

                        ++j;
                    }

                    if (j < 2 || j <= random.nextInt(flag1 ? 5 : 4)) {
                        flag = true;
                    }
                } else if (blockstate.isAir()) {
                    flag = true;
                }

                if (flag && allNeighborsEmpty(worldIn, blockpos, (Direction) null) && worldIn.isEmptyBlock(pos.above(2))) {
                    if (CraftEventFactory.handleBlockSpreadEvent(worldIn, pos, blockpos, this.defaultBlockState().setValue(ChorusFlowerBlock.AGE, i), 2)) {
                        worldIn.setBlock(pos, ChorusPlantBlock.getStateWithConnections(worldIn, pos, this.plant.defaultBlockState()), 2);
                        this.placeGrownFlower(worldIn, blockpos, i);
                    }
                } else if (i < 4) {
                    int l = random.nextInt(4);
                    if (flag1) {
                        ++l;
                    }

                    boolean flag2 = false;

                    for (int i1 = 0; i1 < l; ++i1) {
                        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                        BlockPos blockpos1 = pos.relative(direction);
                        if (worldIn.isEmptyBlock(blockpos1) && worldIn.isEmptyBlock(blockpos1.below()) && allNeighborsEmpty(worldIn, blockpos1, direction.getOpposite())) {
                            if (CraftEventFactory.handleBlockSpreadEvent(worldIn, pos, blockpos1, this.defaultBlockState().setValue(ChorusFlowerBlock.AGE, i + 1), 2)) {
                                this.placeGrownFlower(worldIn, blockpos1, i + 1);
                                flag2 = true;
                            }
                        }
                    }

                    if (flag2) {
                        worldIn.setBlock(pos, ChorusPlantBlock.getStateWithConnections(worldIn, pos, this.plant.defaultBlockState()), 2);
                    } else {
                        if (CraftEventFactory.handleBlockGrowEvent(worldIn, pos, this.defaultBlockState().setValue(ChorusFlowerBlock.AGE, 5), 2)) {
                            this.placeDeadFlower(worldIn, pos);
                        }
                    }
                } else {
                    if (CraftEventFactory.handleBlockGrowEvent(worldIn, pos, this.defaultBlockState().setValue(ChorusFlowerBlock.AGE, 5), 2)) {
                        this.placeDeadFlower(worldIn, pos);
                    }
                }
            }
        }
    }

    @Inject(method = "onProjectileHit", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"))
    private void banner$hitByProjectile(Level p_51654_, BlockState p_51655_, BlockHitResult result, Projectile projectile, CallbackInfo ci) {
        if (!CraftEventFactory.callEntityChangeBlockEvent(projectile, result.getBlockPos(), Blocks.AIR.defaultBlockState())) {
            ci.cancel();
        }
    }
}
