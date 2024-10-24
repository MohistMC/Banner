package org.bukkit.entity;

import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a mushroom {@link Cow}
 */
public interface MushroomCow extends Cow, io.papermc.paper.entity.Shearable { // Paper

    /**
     * Get the variant of this cow.
     *
     * @return cow variant
     */
    @NotNull
    public Variant getVariant();

    /**
     * Set the variant of this cow.
     *
     * @param variant cow variant
     */
    public void setVariant(@NotNull Variant variant);

    // Banner start - since we do not use kyori so override it
    @Override
    default void shear() {
        this.shear(Sound.ENTITY_MOOSHROOM_SHEAR);
    }
    // Banner end

    /**
     * Represents the variant of a cow - ie its color.
     */
    public enum Variant {
        /**
         * Red mushroom cow.
         */
        RED,
        /**
         * Brown mushroom cow.
         */
        BROWN;
    }
}
