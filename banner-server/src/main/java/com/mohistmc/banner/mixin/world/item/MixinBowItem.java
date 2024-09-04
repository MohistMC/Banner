package com.mohistmc.banner.mixin.world.item;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BowItem.class)
public abstract class MixinBowItem extends ProjectileWeaponItem {

    public MixinBowItem(Properties properties) {
        super(properties);
    }

}