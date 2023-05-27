package com.mohistmc.banner.mixin.core.world;

import com.mohistmc.banner.injection.world.InjectionContainer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;

@Mixin(Container.class)
public interface MixinContainer extends InjectionContainer {

    @Override
    default java.util.List<ItemStack> getContents() {
        return Collections.emptyList();
    }

    @Override
    default java.util.List<org.bukkit.entity.HumanEntity> getViewers() {
        return Collections.emptyList();
    }

    @Override
    void onOpen(CraftHumanEntity who);

    @Override
    void onClose(CraftHumanEntity who);

    @Override
    InventoryHolder getOwner();

    @Override
    Location getLocation();

    @Override
    InventoryView getBukkitView();
}
