package com.mohistmc.banner.injection.world.entity;

public interface InjectionAreaEffectCloud {

    default void refreshEffects() {
    }

    default String getPotionType() {
        return null;
    }

    default void setPotionType(String string) {
    }
}
