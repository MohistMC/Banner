package com.mohistmc.banner.mixin.world.entity.player;

import com.mohistmc.banner.injection.world.entity.player.InjectionInventory;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;

//TODO fix inject methods
@Mixin(Inventory.class)
public abstract class MixinInventory implements Container, Nameable, InjectionInventory {
}
