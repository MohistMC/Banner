package org.bukkit.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an Experience Orb.
 */
public interface ExperienceOrb extends Entity {

    /**
     * Gets how much experience is contained within this orb
     *
     * @return Amount of experience
     */
    public int getExperience();

    /**
     * Sets how much experience is contained within this orb
     *
     * @param value Amount of experience
     */
    public void setExperience(int value);

    // Paper start
    /**
     * Check if this orb was spawned from a {@link ThrownExpBottle}
     *
     * @return if orb was spawned from a bottle
     * @deprecated Use getSpawnReason() == EXP_BOTTLE
     */
    @Deprecated
    default boolean isFromBottle() {
        return getSpawnReason() == SpawnReason.EXP_BOTTLE;
    }

    /**
     * Reasons for why this Experience Orb was spawned
     */
    enum SpawnReason {
        /**
         * Spawned by a player dying
         */
        PLAYER_DEATH,
        /**
         * Spawned by an entity dying after being damaged by a player
         */
        ENTITY_DEATH,
        /**
         * Spawned by player using a furnace
         */
        FURNACE,
        /**
         * Spawned by player breeding animals
         */
        BREED,
        /**
         * Spawned by player trading with a villager
         */
        VILLAGER_TRADE,
        /**
         * Spawned by player fishing
         */
        FISHING,
        /**
         * Spawned by player breaking a block that gives experience points such as Diamond Ore
         */
        BLOCK_BREAK,
        /**
         * Spawned by Bukkit API
         */
        CUSTOM,
        /**
         * Spawned by a player throwing an experience points bottle
         */
        EXP_BOTTLE,
        /**
         * Spawned by a player using a grindstone
         */
        GRINDSTONE,
        /**
         * We do not know why it was spawned
         */
        UNKNOWN
    }
}
