package com.destroystokyo.paper.event.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a projectile collides with an entity
 * <p>
 * This event is called <b>before</b> {@link org.bukkit.event.entity.EntityDamageByEntityEvent}, and cancelling it will allow the projectile to continue flying
 * @deprecated Deprecated, use {@link org.bukkit.event.entity.ProjectileHitEvent} and check if there is a hit entity
 */
@Deprecated
public class ProjectileCollideEvent extends EntityEvent implements Cancellable {
    @NotNull private final Entity collidedWith;

    /**
     * Get the entity the projectile collided with
     *
     * @return the entity collided with
     */
    @NotNull
    public Entity getCollidedWith() {
        return collidedWith;
    }

    public ProjectileCollideEvent(@NotNull Projectile what, @NotNull Entity collidedWith) {
        super(what);
        this.collidedWith = collidedWith;
    }

    /**
     * Get the projectile that collided
     *
     * @return the projectile that collided
     */
    @NotNull
    public Projectile getEntity() {
        return (Projectile) super.getEntity();
    }

    private static final HandlerList handlerList = new HandlerList();

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
