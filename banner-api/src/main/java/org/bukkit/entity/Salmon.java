
package org.bukkit.entity;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a salmon fish.
 */
public interface Salmon extends Fish {

    /**
     * Get the variant of this salmon.
     *
     * @return salmon variant
     */
    @NotNull
    public Variant getVariant();

    /**
     * Set the variant of this salmon.
     *
     * @param variant salmon variant
     */
    public void setVariant(@NotNull Variant variant);

    /**
     * Represents the variant of a salmon - ie its size.
     */
    public enum Variant {

        /**
         * Small salmon.
         */
        SMALL,
        /**
         * Default salmon.
         */
        MEDIUM,
        /**
         * Large salmon.
         */
        LARGE;
    }
}
