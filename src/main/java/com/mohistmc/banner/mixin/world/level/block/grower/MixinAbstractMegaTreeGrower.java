package com.mohistmc.banner.mixin.world.level.block.grower;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMegaTreeGrower.class)
public abstract class MixinAbstractMegaTreeGrower extends AbstractTreeGrower {

    @Shadow @Nullable protected abstract ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource random);

    @Inject(method = "placeMega", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;value()Ljava/lang/Object;", shift = At.Shift.AFTER))
    private void banner$setTreeType(ServerLevel level, ChunkGenerator generator, BlockPos pos, BlockState state, RandomSource random, int branchX, int branchY, CallbackInfoReturnable<Boolean> cir) {
        setTreeType( (Holder)level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolder(this.getConfiguredMegaFeature(random)).orElse((Holder.Reference<ConfiguredFeature<?, ?>>) null)); // CraftBukkit
    }
}
