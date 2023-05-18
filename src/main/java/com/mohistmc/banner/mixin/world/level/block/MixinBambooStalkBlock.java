package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.atomic.AtomicReference;

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
        return false;
    }

    @Inject(method = "growBamboo", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            ordinal = 1))
    private void banner$setShouldUpdate(BlockState state, Level level, BlockPos pos, RandomSource random, int age, CallbackInfo ci) {
        shouldUpdateOthers = true;
    }

    @Redirect(method = "growBamboo", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            ordinal = 2))
    private boolean banner$cancelSetBlock1(Level instance, BlockPos pos, BlockState newState, int flags) {
        return false;
    }

    @Inject(method = "growBamboo", at =@At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$gowBambooEvent(BlockState state, Level level, BlockPos pos, RandomSource random, int age, CallbackInfo ci, BlockState blockState, BlockPos blockPos, BlockState blockState2, BambooLeaves bambooLeaves, int i, int j) {
        // CraftBukkit start
        if (CraftEventFactory.handleBlockSpreadEvent(level, pos, pos.above(), (BlockState) ((BlockState) ((BlockState) this.defaultBlockState().setValue(BambooStalkBlock.AGE, j)).setValue(BambooStalkBlock.LEAVES, bambooLeaves)).setValue(BambooStalkBlock.STAGE, j), 3)) {
            if (shouldUpdateOthers) {
                level.setBlock(pos.below(), (BlockState)blockState.setValue(LEAVES, BambooLeaves.SMALL), 3);
                level.setBlock(blockPos, (BlockState)blockState2.setValue(LEAVES, BambooLeaves.NONE), 3);
            }
        }
        // CraftBukkit end
    }

    private AtomicReference<ServerLevel> banner$level = new AtomicReference<>();

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void banner$setLevel(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        banner$level.set(level);
    }

    @ModifyConstant(method = "randomTick", constant = @Constant(intValue = 3))
    private int banner$corpRate(int constant) {
        return banner$level.get().bridge$spigotConfig().bambooModifier / 100;
    }
}
