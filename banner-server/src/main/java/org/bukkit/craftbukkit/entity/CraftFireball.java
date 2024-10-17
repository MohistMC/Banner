package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Fireball;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class CraftFireball extends AbstractProjectile implements Fireball {
    public CraftFireball(CraftServer server, AbstractHurtingProjectile entity) {
        super(server, entity);
    }

    @Override
    public float getYield() {
        return this.getHandle().bridge$bukkitYield();
    }

    @Override
    public boolean isIncendiary() {
        return this.getHandle().bridge$isIncendiary();
    }

    @Override
    public void setIsIncendiary(boolean isIncendiary) {
        this.getHandle().banner$setIsIncendiary(isIncendiary);
    }

    @Override
    public void setYield(float yield) {
        this.getHandle().banner$setBukkitYield(yield);
    }

    @Override
    public ProjectileSource getShooter() {
        return this.getHandle().bridge$projectileSource();
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        if (shooter instanceof CraftLivingEntity) {
            this.getHandle().setOwner(((CraftLivingEntity) shooter).getHandle());
        } else {
            this.getHandle().setOwner(null);
        }
        this.getHandle().banner$setProjectileSource(shooter);
    }

    @Override
    public Vector getDirection() {
        return getAcceleration();
    }

    @Override
    public void setDirection(Vector direction) {
        Preconditions.checkArgument(direction != null, "Vector direction cannot be null");
        if (direction.isZero()) {
            setVelocity(direction);
            setAcceleration(direction);
            return;
        }
        direction = direction.clone().normalize();
        setVelocity(direction.clone().multiply(getVelocity().length()));
        setAcceleration(direction.multiply(getAcceleration().length()));
    }

    @Override
    public void setAcceleration(@NotNull Vector acceleration) {
        Preconditions.checkArgument(acceleration != null, "Vector acceleration cannot be null");
        // SPIGOT-6993: EntityFireball#assignPower will normalize the given values
        // Note: Because of MC-80142 the fireball will stutter on the client when setting the power to something other than 0 or the normalized vector * 0.1
        getHandle().assignDirectionalMovement(new Vec3(acceleration.getX(), acceleration.getY(), acceleration.getZ()), acceleration.length());
        update(); // SPIGOT-6579
    }

    @NotNull
    @Override
    public Vector getAcceleration() {
        Vec3 delta = getHandle().getDeltaMovement();
        return new Vector(delta.x, delta.y, delta.z);
    }

    @Override
    public AbstractHurtingProjectile getHandle() {
        return (AbstractHurtingProjectile) this.entity;
    }

    @Override
    public String toString() {
        return "CraftFireball";
    }
}