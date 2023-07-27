package io.papermc.paper.entity;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an entity that can be sheared.
 */
public interface Shearable extends Entity {

    /**
     * Forces the entity to be sheared and then play the effect as if it were sheared by a player.
     * This will cause the entity to be sheared, even if {@link Shearable#readyToBeSheared()} is false.
     * <p>
     * Some shearing behavior may cause the entity to no longer be valid
     * due to it being replaced by a different entity.
     */
    default void shear() {
        this.shear(Sound.ENTITY_SHEEP_SHEAR);
    }

    /**
     * Forces the entity to be sheared and then play the effect as if it were sheared by the provided source.
     * This will cause the entity to be sheared, even if {@link Shearable#readyToBeSheared()} is false.
     * <p>
     * Some shearing behavior may cause the entity to no longer be valid
     * due to it being replaced by a different entity.
     * <p>
     * This simulates the behavior of an actual shearing, which may cause events like EntityTransformEvent to be called
     * for mooshrooms, and EntityDropItemEvent to be called for sheep.
     *
     * @param source Sound source to play any sound effects on
     */
    void shear(@NotNull Sound source);

    /**
     * Gets if the entity would be able to be sheared or not naturally using shears.
     *
     * @return if the entity can be sheared
     */
    boolean readyToBeSheared();
}