package com.mohistmc.banner.paper.addon.attribute;

import org.bukkit.attribute.AttributeModifier;
import org.jetbrains.annotations.NotNull;

public interface AddonAttributeInstance {

    // Paper start  Transient modifier API
    /**
     * Add a transient modifier to this instance.
     * Transient modifiers are not persisted (saved with the NBT data)
     *
     * @param modifier to add
     */
    void addTransientModifier(@NotNull AttributeModifier modifier);
    // Paper end
}
