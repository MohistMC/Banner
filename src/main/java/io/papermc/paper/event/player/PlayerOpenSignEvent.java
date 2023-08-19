package io.papermc.paper.event.player;

import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player begins editing a sign's text.
 * <p>
 * Cancelling this event stops the sign editing menu from opening.
 */
@Deprecated(since = "CraftBukkit added it")
public class PlayerOpenSignEvent extends org.bukkit.event.player.PlayerSignOpenEvent { // Due to CraftBukkit implements this event, extend it

    public PlayerOpenSignEvent(@NotNull Player player, @NotNull Sign sign, @NotNull Side side, @NotNull Cause cause) {
        super(player, sign, side, cause);
    }
}