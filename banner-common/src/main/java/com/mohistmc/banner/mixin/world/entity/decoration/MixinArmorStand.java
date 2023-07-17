package com.mohistmc.banner.mixin.world.entity.decoration;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ArmorStand.class)
public abstract class MixinArmorStand extends LivingEntity {

    protected MixinArmorStand(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public float getBukkitYaw() {
        return this.getYRot();
    }

    @Override
    public boolean shouldDropExperience() {
        return true;// MC-157395, SPIGOT-5193 even baby (small) armor stands should drop
    }
}
