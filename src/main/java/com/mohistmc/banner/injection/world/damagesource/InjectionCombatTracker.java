package com.mohistmc.banner.injection.world.damagesource;

import net.minecraft.network.chat.Component;

public interface InjectionCombatTracker {

    default void banner$setDeathMessage(Component component) {
        throw new IllegalStateException("Not implemented");
    }
}
