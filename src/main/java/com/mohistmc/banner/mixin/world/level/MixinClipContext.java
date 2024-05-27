package com.mohistmc.banner.mixin.world.level;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ClipContext.class, priority = 1500)
public class MixinClipContext {

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/shapes/CollisionContext;of(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/shapes/CollisionContext;"))
    private CollisionContext banner$resetClipContext(Entity entity) {
        return (entity == null) ? CollisionContext.empty() : CollisionContext.of(entity); // CraftBukkit;
    }
}
