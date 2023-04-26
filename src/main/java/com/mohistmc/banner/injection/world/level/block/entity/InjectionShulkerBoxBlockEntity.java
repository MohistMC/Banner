package com.mohistmc.banner.injection.world.level.block.entity;

import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;

import java.util.List;

public interface InjectionShulkerBoxBlockEntity {

    default List<HumanEntity> bridge$transaction() {
        return null;
    }

    default void banner$setTransaction(List<HumanEntity> transaction) {
    }

    default boolean bridge$opened() {
        return false;
    }

    default void banner$setOpened(boolean opened) {
    }

    default List<ItemStack> getContents() {
        return null;
    }

    default void onOpen(CraftHumanEntity who) {
    }

    default void onClose(CraftHumanEntity who) {
    }

    default List<HumanEntity> getViewers() {
        return null;
    }

    default void setMaxStackSize(int size) {

    }
}
