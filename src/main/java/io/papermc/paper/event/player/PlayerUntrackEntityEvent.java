package io.papermc.paper.event.player;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Is called when a {@link Player} untracks an {@link Entity}.
 */
public class PlayerUntrackEntityEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;

    public PlayerUntrackEntityEvent(@NotNull Player player, @NotNull Entity entity) {
        super(player);
        this.entity = entity;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the entity that will be untracked
     * @return the entity untracked
     */
    @NotNull
    public Entity getEntity() {
        return entity;
    }
}
