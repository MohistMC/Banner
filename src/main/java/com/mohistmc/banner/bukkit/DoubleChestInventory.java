package com.mohistmc.banner.bukkit;

import net.minecraft.network.chat.Component;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.jetbrains.annotations.Nullable;

public class DoubleChestInventory implements MenuProvider {
    private final ChestBlockEntity tileentitychest;
    private final ChestBlockEntity tileentitychest1;
    public final CompoundContainer inventorylargechest;

    public DoubleChestInventory(ChestBlockEntity tileentitychest, ChestBlockEntity tileentitychest1, CompoundContainer inventorylargechest) {
        this.tileentitychest = tileentitychest;
        this.tileentitychest1 = tileentitychest1;
        this.inventorylargechest = inventorylargechest;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (tileentitychest.canOpen(player) && tileentitychest1.canOpen(player)) {
            tileentitychest.unpackLootTable(inventory.player);
            tileentitychest1.unpackLootTable(inventory.player);
            return ChestMenu.sixRows(i, inventory, inventorylargechest);
        } else {
            return null;
        }
    }

    @Override
    public Component getDisplayName() {
        return (Component) (tileentitychest.hasCustomName()
                ? tileentitychest.getDisplayName()
                : (tileentitychest1.hasCustomName()
                ? tileentitychest1.getDisplayName()
                : Component.translatable("container.chestDouble")));
    }
}