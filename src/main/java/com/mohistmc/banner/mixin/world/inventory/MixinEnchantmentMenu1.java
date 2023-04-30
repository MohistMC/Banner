package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/inventory/EnchantmentMenu$1")
public abstract class MixinEnchantmentMenu1 implements Container {

    @Shadow(aliases = {"field_7815"}, remap = false) private EnchantmentMenu outerThis;

    @Override
    public Location getLocation() {
        return outerThis.access.getLocation();
    }
}
