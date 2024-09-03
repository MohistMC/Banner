package com.mohistmc.banner.injection.world.entity;

import net.minecraft.world.item.Item;

import java.util.Set;

public interface InjectionPiglin {

    default Set<Item> bridge$allowedBarterItems() {
        throw new IllegalStateException("Not implemented");
    }

    default Set<Item> bridge$interestItems() {
        throw new IllegalStateException("Not implemented");
    }
}
