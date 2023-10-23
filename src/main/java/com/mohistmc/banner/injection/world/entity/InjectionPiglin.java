package com.mohistmc.banner.injection.world.entity;

import java.util.Set;
import net.minecraft.world.item.Item;

public interface InjectionPiglin {

    default Set<Item> bridge$allowedBarterItems() {
        return null;
    }

    default Set<Item> bridge$interestItems() {
        return null;
    }
}
