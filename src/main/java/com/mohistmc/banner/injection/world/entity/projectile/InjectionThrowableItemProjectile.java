package com.mohistmc.banner.injection.world.entity.projectile;

import net.minecraft.world.item.Item;

public interface InjectionThrowableItemProjectile {

    default Item getDefaultItemPublic() {
        throw new IllegalStateException("Not implemented");
    }
}
