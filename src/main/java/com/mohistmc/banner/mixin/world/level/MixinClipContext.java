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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(ClipContext.class)
public class MixinClipContext {

    @Mutable
    @Shadow @Final private CollisionContext collisionContext;

    private AtomicReference<Entity> banner$entity = new AtomicReference<>();

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/ClipContext;from:Lnet/minecraft/world/phys/Vec3;",
            shift = At.Shift.BEFORE))
    private void banner$setEntity(Vec3 vec3, Vec3 vec32, ClipContext.Block block, ClipContext.Fluid fluid, Entity entity, CallbackInfo ci) {
        banner$entity.set(entity);
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/level/ClipContext;collisionContext:Lnet/minecraft/world/phys/shapes/CollisionContext;"))
    private void banner$resetRayTrace(ClipContext instance, CollisionContext value) {
        this.collisionContext = (banner$entity.get() == null) ? CollisionContext.empty() : CollisionContext.of(banner$entity.get()); // CraftBukkit
    }
}
