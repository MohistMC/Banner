package com.mohistmc.banner.injection.world.food;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

public interface InjectionFoodData {

    default int bridge$saturatedRegenRate() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setSaturatedRegenRate(int saturatedRegenRate) {
        throw new IllegalStateException("Not implemented");
    }

    default int bridge$unsaturatedRegenRate() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setUnsaturatedRegenRate(int unsaturatedRegenRate) {
        throw new IllegalStateException("Not implemented");
    }

    default int bridge$starvationRate() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setStarvationRate(int starvationRate) {
        throw new IllegalStateException("Not implemented");
    }

    default Player getEntityhuman() {
        throw new IllegalStateException("Not implemented");
    }

    default void setEntityhuman(Player entityhuman) {
        throw new IllegalStateException("Not implemented");
    }

    default void eat(ItemStack itemstack, FoodProperties foodinfo) {
        throw new IllegalStateException("Not implemented");
    }

    default void pushEatStack(ItemStack stack) {
        throw new IllegalStateException("Not implemented");
    }
}
