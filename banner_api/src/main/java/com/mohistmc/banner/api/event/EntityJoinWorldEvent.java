package com.mohistmc.banner.api.event;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntityJoinWorldEvent extends EntityEvent implements Cancellable {

    private boolean cancel = false;

    private final World world;
    private final boolean loadedFromDisk;
    private static final HandlerList handlers = new HandlerList();

    public EntityJoinWorldEvent(Entity entity, World world) {
        this(entity, world, false);
    }

    public EntityJoinWorldEvent(Entity entity, World world, boolean loadedFromDisk) {
        super(entity);
        this.world = world;
        this.loadedFromDisk = loadedFromDisk;
    }

    /**
     * {@return the level that the entity is set to join}
     */
    public World getWorld() {
        return world;
    }

    public boolean loadedFromDisk() {
        return loadedFromDisk;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}