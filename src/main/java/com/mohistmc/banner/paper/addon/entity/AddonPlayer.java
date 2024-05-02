package com.mohistmc.banner.paper.addon.entity;

import org.bukkit.entity.LivingEntity;

public interface AddonPlayer {

    // Paper start
    /**
     * Shows the player the win screen that normally is only displayed after one kills the ender dragon
     * and exits the end for the first time.
     * In vanilla, the win screen starts with a poem and then continues with the credits but its content can be
     * changed by using a resource pack.
     * <br>
     * Calling this method does not change the value of {@link #hasSeenWinScreen()}.
     * That means that the win screen is still displayed to a player if they leave the end for the first time, even though
     * they have seen it before because this method was called.
     * Note this method does not make the player invulnerable, which is normally expected when viewing credits.
     *
     * @see #hasSeenWinScreen()
     * @see #setHasSeenWinScreen(boolean)
     * @see <a href="https://minecraft.fandom.com/wiki/End_Poem#Technical_details">https://minecraft.fandom.com/wiki/End_Poem#Technical_details</a>
     */
    public void showWinScreen();

    /**
     * Returns whether this player has seen the win screen before.
     * When a player leaves the end the win screen is shown to them if they have not seen it before.
     *
     * @return Whether this player has seen the win screen before
     * @see #setHasSeenWinScreen(boolean)
     * @see #showWinScreen()
     * @see <a href="https://minecraft.fandom.com/wiki/End_Poem">https://minecraft.fandom.com/wiki/End_Poem</a>
     */
    public boolean hasSeenWinScreen();

    /**
     * Changes whether this player has seen the win screen before.
     * When a player leaves the end the win screen is shown to them if they have not seen it before.
     *
     * @param hasSeenWinScreen Whether this player has seen the win screen before
     * @see #hasSeenWinScreen()
     * @see #showWinScreen()
     * @see <a href="https://minecraft.fandom.com/wiki/End_Poem">https://minecraft.fandom.com/wiki/End_Poem</a>
     */
    public void setHasSeenWinScreen(boolean hasSeenWinScreen);
    // Paper end

    // Paper start
    /**
     * Get whether the player can affect mob spawning
     *
     * @return if the player can affect mob spawning
     */
    public boolean getAffectsSpawning();

    /**
     * Set whether the player can affect mob spawning
     *
     * @param affects Whether the player can affect mob spawning
     */
    public void setAffectsSpawning(boolean affects);
    // Paper end
}
