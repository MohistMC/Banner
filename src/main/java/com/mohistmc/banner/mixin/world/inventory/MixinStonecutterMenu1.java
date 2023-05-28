package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.StonecutterMenu;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/inventory/StonecutterMenu$1")
public abstract class MixinStonecutterMenu1 implements Container {

    @Shadow(aliases = {"field_17637"}, remap = false) private StonecutterMenu outerThis;

    @Override
    public Location getLocation() {
        return outerThis.access.getLocation();
    }
}
