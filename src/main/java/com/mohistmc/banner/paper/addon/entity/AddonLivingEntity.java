package com.mohistmc.banner.paper.addon.entity;

import org.bukkit.Location;

public interface AddonLivingEntity {

    // Paper start - LivingEntity#clearActivePotionEffects();
    /**
     * Removes all active potion effects for this entity.
     *
     * @return true if any were removed
     */
    boolean clearActivePotionEffects();

    /**
     * Gets entity body yaw
     *
     * @return entity body yaw
     * @see Location#getYaw()
     */
    float getBodyYaw();

    /**
     * Sets entity body yaw
     *
     * @param bodyYaw new entity body yaw
     * @see Location#setYaw(float)
     */
    void setBodyYaw(float bodyYaw);
    // Paper end

}
