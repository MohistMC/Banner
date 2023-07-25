package com.mohistmc.banner.mixin.world.level;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClipContext.class)
public class MixinClipContext {

    @Mutable
    @Shadow @Final private CollisionContext collisionContext;

    @Inject(method = "<init>", at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$resetClipContext(Vec3 from, Vec3 _to, ClipContext.Block block, ClipContext.Fluid fluid,
                                         Entity entity, CallbackInfo ci) {
        this.collisionContext = (entity == null) ? CollisionContext.empty() : CollisionContext.of(entity); // CraftBukkit
    }
}
