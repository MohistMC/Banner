package com.mohistmc.banner.paper.addon.entity.monster;

public interface AddonSlime {

    // Paper start

    /**
     * Get whether this slime can randomly wander/jump around on its own
     *
     * @return true if can wander
     */
    public boolean canWander();

    /**
     * Set whether this slime can randomly wander/jump around on its own
     *
     * @param canWander true if can wander
     */
    public void setWander(boolean canWander);
    // Paper end
}
