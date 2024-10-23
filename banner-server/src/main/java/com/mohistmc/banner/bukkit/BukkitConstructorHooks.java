package com.mohistmc.banner.bukkit;

import net.minecraft.commands.Commands;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.bukkit.craftbukkit.inventory.CraftMerchantRecipe;

import java.util.Optional;

public class BukkitConstructorHooks {

    public static Commands newCommands() {
        Commands commands;
        try {
            commands = Commands.class.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return commands;
    }

    public static LecternMenu newLecternMenu(int i, Container container, net.minecraft.world.entity.player.Inventory bottom) {
        LecternMenu lecternMenu = new LecternMenu(i, container,  new SimpleContainerData(1));
        lecternMenu.bridge$setPlayerInventory(bottom);
        return lecternMenu;
    }

    public static MerchantOffer newMerchantOffer(ItemCost itemCost, Optional<ItemCost> optional, ItemStack itemStack, int i, int j, int k, float f, int l, CraftMerchantRecipe bukkit) {
        MerchantOffer merchantOffer = new MerchantOffer(itemCost, optional, itemStack, i, j, k, f, l);
        merchantOffer.banner$setCraftMerchantRecipe(bukkit);
        return merchantOffer;
    }
}