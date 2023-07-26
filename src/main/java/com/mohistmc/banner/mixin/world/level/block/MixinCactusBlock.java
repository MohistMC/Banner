package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(CactusBlock.class)
public class MixinCactusBlock {

    @Inject(method = "entityInside", at = @At("HEAD"))
    private void banner$cactusDamage1(BlockState state, Level worldIn, BlockPos pos, Entity entityIn, CallbackInfo ci) {
        CraftEventFactory.blockDamage = CraftBlock.at(worldIn, pos);
    }

    @Inject(method = "entityInside", at = @At("RETURN"))
    private void banner$cactusDamage2(BlockState state, Level worldIn, BlockPos pos, Entity entityIn, CallbackInfo ci) {
        CraftEventFactory.blockDamage = null;
    }

    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean banner$blockGrow(ServerLevel serverWorld, BlockPos pos, BlockState state) {
        return CraftEventFactory.handleBlockGrowEvent(serverWorld, pos, state);
    }

    private AtomicReference<ServerLevel> banner$level = new AtomicReference<>();

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void banner$setLevel(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        banner$level.set(level);
    }

    @ModifyConstant(method = "randomTick", constant = @Constant(intValue = 3))
    private int banner$cactusHeight(int constant) {
        return banner$level.get().bridge$bannerConfig().cactusHeight;// Paper - Configurable growth height
    }
}
