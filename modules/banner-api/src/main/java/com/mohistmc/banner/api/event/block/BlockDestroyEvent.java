package com.mohistmc.banner.api.event.block;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

public class BlockDestroyEvent extends BlockEvent implements Cancellable {

    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final Location location;

    public BlockDestroyEvent(Location location, Entity entity) {
        super(location.getBlock());
        this.location = location;
        this.entity = entity;
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

    public Location getLocation() {
        return location;
    }

    public Entity getEntity() {
        return entity;
    }
}
