package com.mohistmc.banner.injection.world.food;

import net.minecraft.world.entity.player.Player;

public interface InjectionFoodData {

    default int bridge$saturatedRegenRate() {
        return 0;
    }

    default void banner$setSaturatedRegenRate(int saturatedRegenRate) {
    }

    default int bridge$unsaturatedRegenRate() {
        return 0;
    }

    default void banner$setUnsaturatedRegenRate(int unsaturatedRegenRate) {
    }

    default int bridge$starvationRate() {
        return 0;
    }

    default void banner$setStarvationRate(int starvationRate) {
    }

    default Player getEntityhuman() {
        return null;
    }

    default void setEntityhuman(Player entityhuman) {
    }
}
