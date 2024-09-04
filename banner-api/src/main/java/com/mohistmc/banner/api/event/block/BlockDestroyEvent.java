package com.mohistmc.banner.api.event.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

public class BlockDestroyEvent extends BlockEvent implements Cancellable {

    private boolean cancel = false;
    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;

    public BlockDestroyEvent(@NotNull Block theBlock, Entity entity) {
        super(theBlock);
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

    public Entity getEntity() {
        return entity;
    }
}
