package org.bukkit.entity;

import org.bukkit.Sound;

/**
 * Represents a snowman entity
 */
public interface Snowman extends Golem, io.papermc.paper.entity.Shearable { // Paper

    /**
     * Gets whether this snowman is in "derp mode", meaning it is not wearing a
     * pumpkin.
     *
     * @return True if the snowman is bald, false if it is wearing a pumpkin
     */
    boolean isDerp();

    /**
     * Sets whether this snowman is in "derp mode", meaning it is not wearing a
     * pumpkin. NOTE: This value is not persisted to disk and will therefore
     * reset when the chunk is reloaded.
     *
     * @param derpMode True to remove the pumpkin, false to add a pumpkin
     */
    void setDerp(boolean derpMode);

    // Banner start - since we do not use kyori so override it
    @Override
    default void shear() {
        this.shear(Sound.ENTITY_SNOW_GOLEM_SHEAR);
    }
    // Banner end
}
