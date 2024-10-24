package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Rabbit.class)
public abstract class MixinRabbit extends Animal {

    @Shadow public abstract void setSpeedModifier(double speedModifier);

    protected MixinRabbit(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }
}
