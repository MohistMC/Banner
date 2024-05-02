package com.mohistmc.banner.mixin.core.world.entity.decoration;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(HangingEntity.class)
public abstract class MixinHangingEntity extends Entity {

    public MixinHangingEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 100))
    private int banner$modifyTick(int constant) {
        return this.level().bridge$spigotConfig().hangingTickFrequency;
    }
}
