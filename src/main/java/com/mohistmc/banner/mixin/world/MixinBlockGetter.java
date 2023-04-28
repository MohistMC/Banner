package com.mohistmc.banner.mixin.world;

import com.mohistmc.banner.injection.world.level.InjectionBlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockGetter.class)
public interface MixinBlockGetter extends InjectionBlockGetter {

    @Shadow FluidState getFluidState(BlockPos pos);

    @Shadow BlockState getBlockState(BlockPos pos);

    @Override
    default BlockHitResult clip(ClipContext context, BlockPos pos) {
        BlockState blockstate = this.getBlockState(pos);
        FluidState ifluidstate = this.getFluidState(pos);
        Vec3 vec3d = context.getFrom();
        Vec3 vec3d1 = context.getFrom();
        VoxelShape voxelshape = context.getBlockShape(blockstate, (BlockGetter) this, pos);
        BlockHitResult blockraytraceresult = ((BlockGetter) this).clipWithInteractionOverride(vec3d, vec3d1, pos, voxelshape, blockstate);
        VoxelShape voxelshape1 = context.getFluidShape(ifluidstate, (BlockGetter) this, pos);
        BlockHitResult blockraytraceresult1 = voxelshape1.clip(vec3d, vec3d1, pos);
        double d0 = blockraytraceresult == null ? Double.MAX_VALUE : context.getFrom().distanceToSqr(blockraytraceresult.getLocation());
        double d1 = blockraytraceresult1 == null ? Double.MAX_VALUE : context.getFrom().distanceToSqr(blockraytraceresult1.getLocation());
        return d0 <= d1 ? blockraytraceresult : blockraytraceresult1;
    }
}
