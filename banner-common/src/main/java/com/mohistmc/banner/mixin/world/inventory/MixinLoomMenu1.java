package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.LoomMenu;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/inventory/LoomMenu$1")
public abstract class MixinLoomMenu1 implements Container {

    @Shadow(aliases = {"field_7851"}, remap = false) private LoomMenu outerThis;

    @Override
    public Location getLocation() {
        return outerThis.access.getLocation();
    }
}
