package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.StackedContentsCompatible;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net/minecraft/world/entity/animal/Sheep$1")
public abstract class MixinSheep1 implements Container, StackedContentsCompatible {

    @Override
    public InventoryView getBukkitView() {
        return null;
    }
}
