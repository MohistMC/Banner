package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MushroomCow.class)
public abstract class MixinMushroomCow extends Cow {

    public MixinMushroomCow(EntityType<? extends Cow> entityType, Level level) {
        super(entityType, level);
    }

    /**
    @Redirect(method = "shear",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
            ordinal = 1))
    private boolean banner$notDrop(Level instance, Entity entity) {
        return false;
    }*/
}
