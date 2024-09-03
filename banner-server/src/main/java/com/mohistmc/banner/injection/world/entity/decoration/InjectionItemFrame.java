package com.mohistmc.banner.injection.world.entity.decoration;

import net.minecraft.world.item.ItemStack;

public interface InjectionItemFrame {

    default void setItem(ItemStack itemstack, boolean flag, boolean playSound) {
        throw new IllegalStateException("Not implemented");
    }
}
