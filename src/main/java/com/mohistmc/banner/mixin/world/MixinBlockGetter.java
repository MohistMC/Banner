package com.mohistmc.banner.mixin.world;

import com.mohistmc.banner.injection.world.level.InjectionBlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.BiFunction;
import java.util.function.Function;

@Mixin(BlockGetter.class)
public interface MixinBlockGetter extends InjectionBlockGetter {

    @Shadow FluidState getFluidState(BlockPos pos);

    @Shadow BlockState getBlockState(BlockPos pos);

    @Shadow @Nullable BlockHitResult clipWithInteractionOverride(Vec3 startVec, Vec3 endVec, BlockPos pos, VoxelShape shape, BlockState state);

    @Shadow
    static <T, C> T traverseBlocks(Vec3 from, Vec3 to, C context, BiFunction<C, BlockPos, T> tester, Function<C, T> onFail) {
        return null;
    }

    // CraftBukkit start - moved block handling into separate method for use by Block#rayTrace
    default BlockHitResult clip(ClipContext raytrace1, BlockPos blockposition) {
        BlockState iblockdata = this.getBlockState(blockposition);
        FluidState fluid = this.getFluidState(blockposition);
        Vec3 vec3d = raytrace1.getFrom();
        Vec3 vec3d1 = raytrace1.getTo();
        VoxelShape voxelshape = raytrace1.getBlockShape(iblockdata, ((BlockGetter) (Object) this), blockposition);
        BlockHitResult movingobjectpositionblock = this.clipWithInteractionOverride(vec3d, vec3d1, blockposition, voxelshape, iblockdata);
        VoxelShape voxelshape1 = raytrace1.getFluidShape(fluid, ((BlockGetter) (Object) this), blockposition);
        BlockHitResult movingobjectpositionblock1 = voxelshape1.clip(vec3d, vec3d1, blockposition);
        double d0 = movingobjectpositionblock == null ? Double.MAX_VALUE : raytrace1.getFrom().distanceToSqr(movingobjectpositionblock.getLocation());
        double d1 = movingobjectpositionblock1 == null ? Double.MAX_VALUE : raytrace1.getFrom().distanceToSqr(movingobjectpositionblock1.getLocation());

        return d0 <= d1 ? movingobjectpositionblock : movingobjectpositionblock1;
    }
    // CraftBukkit end

    /**
     * @author wdog5
     * @reason bukkit patches
     */
    @Overwrite
    default BlockHitResult clip(ClipContext raytrace) {
        return (BlockHitResult) traverseBlocks(raytrace.getFrom(), raytrace.getTo(), raytrace, (raytrace1, blockposition) -> {
            return this.clip(raytrace1, blockposition); // CraftBukkit - moved into separate method
        }, (raytrace1) -> {
            Vec3 vec3d = raytrace1.getFrom().subtract(raytrace1.getTo());

            return BlockHitResult.miss(raytrace1.getTo(), Direction.getNearest(vec3d.x, vec3d.y, vec3d.z), BlockPos.containing(raytrace1.getTo()));
        });
    }

}
