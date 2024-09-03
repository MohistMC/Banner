package com.mohistmc.banner.injection.world.level.block.entity;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface InjectionAbstractFurnaceBlockEntity {

    default Object2IntOpenHashMap<ResourceLocation> getRecipesUsed() {
        throw new IllegalStateException("Not implemented");
    }

    default List<RecipeHolder<?>> getRecipesToAwardAndPopExperience(ServerLevel world, Vec3 vec, BlockPos pos, Player entity, ItemStack itemStack, int amount) {
        throw new IllegalStateException("Not implemented");
    }

    default List<RecipeHolder<?>> bridge$dropExp(ServerPlayer entity, ItemStack itemStack, int amount) {
        throw new IllegalStateException("Not implemented");
    }
}
