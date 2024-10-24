package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.GrindstoneMenu;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/inventory/GrindstoneMenu$1")
public abstract class MixinGrindstoneMenu1 implements Container {

    @Shadow(aliases = {"field_16776"}, remap = false) private GrindstoneMenu outerThis;

    @Override
    public Location getLocation() {
        return outerThis.access.getLocation();
    }
}
