package com.mohistmc.banner.injection.world.damagesource;

import net.minecraft.network.chat.Component;

public interface InjectionCombatEntry {

    default void banner$setDeathMessage(Component component) {
        throw new IllegalStateException("Not implemented");
    }

    default Component bridge$deathMessage() {
        throw new IllegalStateException("Not implemented");
    }
}
