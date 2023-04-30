package com.mohistmc.banner.mixin.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CropBlock.class)
public class MixinCropBlock {

    @Redirect(method = "growCrops(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    public boolean banner$blockGrowGrow(Level world, BlockPos pos, BlockState newState, int flags) {
        return CraftEventFactory.handleBlockGrowEvent(world, pos, newState, flags);
    }

    @ModifyExpressionValue(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    public boolean banner$entityChangeBlock(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
       return !CraftEventFactory.callEntityChangeBlockEvent(entity, pos, Blocks.AIR.defaultBlockState(), !level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)).isCancelled();
    }

    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    public boolean banner$blockGrowTick(ServerLevel world, BlockPos pos, BlockState newState, int flags) {
        return CraftEventFactory.handleBlockGrowEvent(world, pos, newState, flags);
    }

    @Inject(method = "getGrowthSpeed", cancellable = true, at = @At("RETURN"))
    private static void banner$spigotModifier(Block block, BlockGetter blockGetter, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (blockGetter instanceof Level bridge) {
            int modifier;
            if (block == Blocks.BEETROOTS) {
                modifier = bridge.bridge$spigotConfig().beetrootModifier;
            } else if (block ==Blocks.CARROTS) {
                modifier = bridge.bridge$spigotConfig().carrotModifier;
            } else if (block == Blocks.POTATOES) {
                modifier = bridge.bridge$spigotConfig().potatoModifier;
            } else {
                modifier = bridge.bridge$spigotConfig().wheatModifier;
            }
            var f = cir.getReturnValueF();
            f /= (100F / modifier);
            cir.setReturnValue(f);
        }
    }
}
