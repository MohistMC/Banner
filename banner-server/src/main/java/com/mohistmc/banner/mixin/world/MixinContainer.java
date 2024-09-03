package com.mohistmc.banner.mixin.world;

import com.mohistmc.banner.bukkit.inventory.InventoryOwner;
import com.mohistmc.banner.injection.world.InjectionContainer;
import java.util.Collections;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;

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
    default void onOpen(CraftHumanEntity who) {

    }

    @Override
    default void onClose(CraftHumanEntity who) {

    }

    @Override
    default InventoryHolder getOwner() {
        return this instanceof BlockEntity blockEntity ? InventoryOwner.get(blockEntity) : null;
    }

    @Override
    default Location getLocation() {
        if (this instanceof BlockEntity entity) {
            BlockPos blockPos = entity.getBlockPos();
            return new Location(entity.getLevel().getWorld(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }else {
            return null;
        }
    }

    @Override
    InventoryView getBukkitView();
}
