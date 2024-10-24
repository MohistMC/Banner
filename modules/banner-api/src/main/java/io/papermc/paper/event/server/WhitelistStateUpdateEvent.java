package io.papermc.paper.event.server;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;

/**
 * This event gets called when the whitelist status of a player is changed
 */
public class WhitelistStateUpdateEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancel = false;
    @NotNull private final PlayerProfile playerProfile;
    @NotNull private final WhitelistStatus status;

    public WhitelistStateUpdateEvent(@NotNull PlayerProfile who, @NotNull WhitelistStatus status) {
        this.playerProfile = who;
        this.status = status;
    }

    /**
     * Gets the player whose whitelist status is being changed
     *
     * @return the player whose status is being changed
     */
    @NotNull
    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(playerProfile.getUniqueId());
    }

    /**
     * Gets the player profile whose whitelist status is being changed
     *
     * @return the player profile whose status is being changed
     */
    @NotNull
    public PlayerProfile getPlayerProfile() {
        return playerProfile;
    }

    /**
     * Gets the status change of the player profile
     *
     * @return the whitelist status
     */
    @NotNull
    public WhitelistStatus getStatus() {
        return status;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * Enum for the whitelist status changes
     */
    public enum WhitelistStatus {
        ADDED, REMOVED
    }
}
