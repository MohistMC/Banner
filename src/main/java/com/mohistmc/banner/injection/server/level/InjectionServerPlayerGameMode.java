package com.mohistmc.banner.injection.server.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface InjectionServerPlayerGameMode {

    default boolean bridge$isFiredInteract() {
        return false;
    }

    default void bridge$setFiredInteract(boolean firedInteract) {
    }

    default boolean bridge$getInteractResult() {
        return false;
    }

    default BlockPos bridge$getinteractPosition() {
        return null;
    }

    default InteractionHand bridge$getinteractHand() {
        return null;
    }

    default ItemStack bridge$getinteractItemStack() {
        return null;
    }
}
