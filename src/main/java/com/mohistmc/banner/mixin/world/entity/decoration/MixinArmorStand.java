package com.mohistmc.banner.mixin.world.entity.decoration;

import com.mohistmc.banner.injection.world.entity.decoration.InjectionArmorStand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ArmorStand.class)
public abstract class MixinArmorStand extends LivingEntity implements InjectionArmorStand {

    public boolean canMove = true; // Paper

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

    // Paper start
    @Override
    public void move(MoverType type, Vec3 pos) {
        if (this.canMove) {
            super.move(type, pos);
        }
    }
    // Paper end


    @Override
    public boolean bridge$canMove() {
        return canMove;
    }

    @Override
    public void banner$setCanMove(boolean canMove) {
        this.canMove = canMove;
    }
}
