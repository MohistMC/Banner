package com.mohistmc.banner.injection.world.entity;

import net.minecraft.world.item.Item;

import java.util.Set;

public interface InjectionPiglin {

    default Set<Item> bridge$allowedBarterItems() {
        return null;
    }

    default Set<Item> bridge$interestItems() {
        return null;
    }
}
