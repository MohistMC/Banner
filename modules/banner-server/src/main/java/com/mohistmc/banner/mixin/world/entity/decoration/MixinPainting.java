package com.mohistmc.banner.mixin.world.entity.decoration;

import com.mohistmc.banner.asm.annotation.TransformAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Painting.class)
public abstract class MixinPainting {

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static AABB calculateBoundingBoxStatic(BlockPos blockposition, Direction enumdirection, int width, int height) {
        // CraftBukkit end
        float f = 0.46875F;
        Vec3 vec3d = Vec3.atCenterOf(blockposition).relative(enumdirection, -0.46875D);
        // CraftBukkit start
        double d0 = a(width);
        double d1 = a(height);
        // CraftBukkit end
        Direction enumdirection1 = enumdirection.getCounterClockWise();
        Vec3 vec3d1 = vec3d.relative(enumdirection1, d0).relative(Direction.UP, d1);
        Direction.Axis enumdirection_enumaxis = enumdirection.getAxis();
        // CraftBukkit start
        double d2 = enumdirection_enumaxis == Direction.Axis.X ? 0.0625D : (double) width;
        double d3 = (double) height;
        double d4 = enumdirection_enumaxis == Direction.Axis.Z ? 0.0625D : (double) width;
        // CraftBukkit end

        return AABB.ofSize(vec3d1, d2, d3, d4);
    }

    private static double a(int i) {
        return i % 2 == 0 ? 0.5 : 0.0;
    }
}
