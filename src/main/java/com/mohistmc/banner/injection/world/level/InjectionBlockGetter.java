package com.mohistmc.banner.injection.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;

public interface InjectionBlockGetter {

    default BlockHitResult clip(ClipContext raytrace1, BlockPos blockposition) {
        throw new IllegalStateException("Not implemented");
    }
}
