package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.CartographyTableMenu;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/inventory/CartographyTableMenu$1")
public abstract class MixinCartographyTableMenu1 implements Container {
    @Shadow(aliases = {"field_17298"}, remap = false) private CartographyTableMenu outerThis;

    @Override
    public Location getLocation() {
        return outerThis.access.getLocation();
    }
}
