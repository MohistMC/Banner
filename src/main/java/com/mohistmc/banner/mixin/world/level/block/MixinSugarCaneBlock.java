package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(SugarCaneBlock.class)
public class MixinSugarCaneBlock{

    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    public boolean banner$cropGrow(ServerLevel world, BlockPos pos, BlockState state) {
        return CraftEventFactory.handleBlockGrowEvent(world, pos, state);
    }

    private AtomicReference<ServerLevel> banner$level = new AtomicReference<>();

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void banner$setLevel(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        banner$level.set(level);
    }

    @ModifyConstant(method = "randomTick", constant = @Constant(intValue = 3))
    private int banner$reedsHeight(int constant) {
        return banner$level.get().bridge$bannerConfig().reedHeight;// Paper - Configurable growth height
    }
}
