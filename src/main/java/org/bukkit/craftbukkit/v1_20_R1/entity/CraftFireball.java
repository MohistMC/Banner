package org.bukkit.craftbukkit.v1_20_R1.entity;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class CraftFireball extends AbstractProjectile implements Fireball {
    public CraftFireball(CraftServer server, net.minecraft.world.entity.projectile.AbstractHurtingProjectile entity) {
        super(server, entity);
    }

    @Override
    public float getYield() {
        return getHandle().bridge$bukkitYield();
    }

    @Override
    public boolean isIncendiary() {
        return getHandle().bridge$isIncendiary();
    }

    @Override
    public void setIsIncendiary(boolean isIncendiary) {
        getHandle().banner$setIsIncendiary(isIncendiary);
    }

    @Override
    public void setYield(float yield) {
        getHandle().banner$setBukkitYield(yield);
    }

    @Override
    public ProjectileSource getShooter() {
        return getHandle().bridge$projectileSource();
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        if (shooter instanceof CraftLivingEntity) {
            getHandle().setOwner(((CraftLivingEntity) shooter).getHandle());
        } else {
            getHandle().setOwner(null);
        }
        getHandle().banner$setProjectileSource(shooter);
    }

    @Override
    public Vector getDirection() {
        return new Vector(getHandle().xPower, getHandle().yPower, getHandle().zPower);
    }

    @Override
    public void setDirection(Vector direction) {
        Preconditions.checkArgument(direction != null, "Vector direction cannot be null");
        getHandle().setDirection(direction.getX(), direction.getY(), direction.getZ());
        update(); // SPIGOT-6579
    }

    @Override
    public net.minecraft.world.entity.projectile.AbstractHurtingProjectile getHandle() {
        return (net.minecraft.world.entity.projectile.AbstractHurtingProjectile) entity;
    }

    @Override
    public String toString() {
        return "CraftFireball";
    }

    @Override
    public EntityType getType() {
        return EntityType.UNKNOWN;
    }
}
