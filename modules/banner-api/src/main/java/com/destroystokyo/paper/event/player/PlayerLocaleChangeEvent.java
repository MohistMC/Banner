package com.destroystokyo.paper.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Called when the locale of the player is changed.
 *
 * @deprecated Replaced by {@link org.bukkit.event.player.PlayerLocaleChangeEvent} upstream
 */
@Deprecated(forRemoval = true) @ApiStatus.ScheduledForRemoval(inVersion = "1.21")
public class PlayerLocaleChangeEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final String oldLocale;
    private final String newLocale;

    public PlayerLocaleChangeEvent(final Player player, final String oldLocale, final String newLocale) {
        super(player);
        this.oldLocale = oldLocale;
        this.newLocale = newLocale;
    }

    /**
     * Gets the locale the player switched from.
     *
     * @return player's old locale
     */
    public String getOldLocale() {
        return oldLocale;
    }

    /**
     * Gets the locale the player is changed to.
     *
     * @return player's new locale
     */
    public String getNewLocale() {
        return newLocale;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
