package com.mohistmc.banner.injection.world.level.block.entity;

import java.util.List;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.phys.Vec3;

public interface InjectionAbstractFurnaceBlockEntity {

    default Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> getRecipesUsed() {
        throw new IllegalStateException("Not implemented");
    }

    default List<RecipeHolder<?>> getRecipesToAwardAndPopExperience(ServerLevel world, Vec3 vec, BlockPos pos, Player entity, ItemStack itemStack, int amount) {
        throw new IllegalStateException("Not implemented");
    }

    default List<RecipeHolder<?>> bridge$dropExp(ServerPlayer entity, ItemStack itemStack, int amount) {
        throw new IllegalStateException("Not implemented");
    }
}
