package com.mohistmc.banner.mixin.world.level.block.grower;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractMegaTreeGrower.class)
public abstract class MixinAbstractMegaTreeGrower extends AbstractTreeGrower {

    @Inject(method = "placeMega",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$setTreeType(ServerLevel level, ChunkGenerator generator, BlockPos pos,
                                    BlockState state, RandomSource random, int branchX,
                                    int branchY, CallbackInfoReturnable<Boolean> cir,
                                    ResourceKey resourceKey, Holder holder, ConfiguredFeature configuredFeature) {
        setTreeType(holder); // CraftBukkit
    }
}
