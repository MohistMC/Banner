package com.mohistmc.banner.injection.server.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface InjectionServerPlayerGameMode {

    default boolean bridge$isFiredInteract() {
        throw new IllegalStateException("Not implemented");
    }

    default void bridge$setFiredInteract(boolean firedInteract) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean bridge$getInteractResult() {
        throw new IllegalStateException("Not implemented");
    }

    default BlockPos bridge$getinteractPosition() {
        throw new IllegalStateException("Not implemented");
    }

    default InteractionHand bridge$getinteractHand() {
        throw new IllegalStateException("Not implemented");
    }

    default ItemStack bridge$getinteractItemStack() {
        throw new IllegalStateException("Not implemented");
    }
}
