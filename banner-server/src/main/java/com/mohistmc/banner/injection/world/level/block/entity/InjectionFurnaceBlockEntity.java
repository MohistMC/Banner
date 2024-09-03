package com.mohistmc.banner.injection.world.level.block.entity;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;

import java.util.List;

public interface InjectionFurnaceBlockEntity {

    default List<ItemStack> getContents() {
        throw new IllegalStateException("Not implemented");
    }

    default void onOpen(CraftHumanEntity who) {
        throw new IllegalStateException("Not implemented");
    }

    default void onClose(CraftHumanEntity who) {
        throw new IllegalStateException("Not implemented");
    }

    default Object2IntOpenHashMap<ResourceLocation> getRecipesUsed() {
        throw new IllegalStateException("Not implemented");
    }

    default List<HumanEntity> getViewers() {
        throw new IllegalStateException("Not implemented");
    }

    default void setMaxStackSize(int size) {
        throw new IllegalStateException("Not implemented");
    }

    default void awardUsedRecipesAndPopExperience(ServerPlayer entityplayer, ItemStack itemstack, int amount) { // CraftBukkit
        throw new IllegalStateException("Not implemented");
    }

    default List<Recipe<?>> getRecipesToAwardAndPopExperience(ServerLevel worldserver, Vec3 vec3d, BlockPos blockposition, ServerPlayer entityplayer, ItemStack itemstack, int amount) {
        throw new IllegalStateException("Not implemented");
    }
}
