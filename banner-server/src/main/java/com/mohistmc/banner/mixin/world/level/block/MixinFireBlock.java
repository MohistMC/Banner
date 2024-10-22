package com.mohistmc.banner.mixin.world.level.block;

import com.mohistmc.banner.injection.world.level.block.InjectionFireBlock;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.CraftBlockStates;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public abstract class MixinFireBlock implements InjectionFireBlock {


    // @formatter:off
    @Shadow
    protected abstract BlockState getStateForPlacement(BlockGetter blockReader, BlockPos pos);
    // @formatter:on

    @Shadow @Final private Object2IntMap<Block> burnOdds;
    private AtomicReference<BlockPos> sourceposition = new AtomicReference<>();

    @Redirect(method = "tick", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    public boolean banner$fireSpread(ServerLevel world, BlockPos mutablePos, BlockState newState, int flags,
                                       BlockState state, ServerLevel worldIn, BlockPos pos) {
        if (world.getBlockState(mutablePos).getBlock() != Blocks.FIRE) {
            if (!CraftEventFactory.callBlockIgniteEvent(world, mutablePos, pos).isCancelled()) {
                return CraftEventFactory.handleBlockSpreadEvent(world, pos, mutablePos, newState, flags);
            }
        }
        return false;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    public boolean banner$extinguish1(ServerLevel world, BlockPos pos, boolean isMoving) {
        if (!CraftEventFactory.callBlockFadeEvent(world, pos, Blocks.AIR.defaultBlockState()).isCancelled()) {
            world.removeBlock(pos, isMoving);
        }
        return false;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/FireBlock;checkBurnOut(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/util/RandomSource;I)V",
            ordinal = 0, shift = At.Shift.BEFORE))
    private void banner$checkSource0(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        sourceposition.set(pos);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/FireBlock;checkBurnOut(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/util/RandomSource;I)V",
            ordinal = 5, shift = At.Shift.AFTER))
    private void banner$checkSource1(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        sourceposition.set(null);
    }

    @Inject(method = "checkBurnOut", cancellable = true, at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    public void banner$blockBurn(Level level, BlockPos pos, int chance, RandomSource random, int age, CallbackInfo ci) {
        org.bukkit.block.Block theBlock = level.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        org.bukkit.block.Block sourceBlock = level.getWorld().getBlockAt(sourceposition.get().getX(), sourceposition.get().getY(), sourceposition.get().getZ());
        BlockBurnEvent event = new BlockBurnEvent(theBlock, sourceBlock);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
        if (level.getBlockState(pos).getBlock() instanceof TntBlock && !CraftEventFactory.callTNTPrimeEvent(level, pos, TNTPrimeEvent.PrimeCause.FIRE, null, sourceposition.get())) {
            ci.cancel();
        }
    }

    @Redirect(method = "updateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;"))
    public BlockState banner$blockFade(net.minecraft.world.level.block.Block block, BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!(worldIn instanceof Level)) {
            return Blocks.AIR.defaultBlockState();
        }
        CraftBlockState blockState = CraftBlockStates.getBlockState(worldIn, currentPos);
        blockState.setData(Blocks.AIR.defaultBlockState());
        BlockFadeEvent event = new BlockFadeEvent(blockState.getBlock(), blockState);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return this.getStateForPlacement(worldIn, currentPos).setValue(FireBlock.AGE, stateIn.getValue(FireBlock.AGE));
        } else {
            return blockState.getHandle();
        }
    }

    @Override
    public boolean bridge$canBurn(Block block) {
        return this.burnOdds.containsKey(block);
    }
}
