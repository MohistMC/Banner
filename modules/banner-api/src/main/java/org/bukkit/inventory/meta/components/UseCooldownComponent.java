package org.bukkit.inventory.meta.components;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a component which determines the cooldown applied to use of this
 * item.
 */
@ApiStatus.Experimental
public interface UseCooldownComponent extends ConfigurationSerializable {

    /**
     * Gets the time in seconds it will take for this item to be eaten.
     *
     * @return eat time
     */
    float getCooldownSeconds();

    /**
     * Sets the time in seconds it will take for this item to be eaten.
     *
     * @param eatSeconds new eat time, must be positive
     */
    void setCooldownSeconds(float eatSeconds);

    /**
     * Gets the custom cooldown group to be used for similar items, if set.
     *
     * @return the cooldown group
     */
    @Nullable
    NamespacedKey getCooldownGroup();

    /**
     * Sets the custom cooldown group to be used for similar items.
     *
     * @param song the cooldown group
     */
    void setCooldownGroup(@Nullable NamespacedKey song);
}
