package com.mohistmc.banner.mixin.core.world.item.crafting;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerRecipeBook.class)
public abstract class MixinServerRecipeBook extends RecipeBook {

    @Shadow protected abstract void sendRecipes(ClientboundRecipePacket.State state, ServerPlayer player, List<ResourceLocation> recipes);

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public int addRecipes(Collection<RecipeHolder<?>> recipeHolders, ServerPlayer serverPlayer) {
        List<ResourceLocation> list = Lists.newArrayList();
        int i = 0;

        for (RecipeHolder<?> recipeholder : recipeHolders) {
            ResourceLocation resourcelocation = recipeholder.id();
            if (!this.known.contains(resourcelocation) && !recipeholder.value().isSpecial() && CraftEventFactory.handlePlayerRecipeListUpdateEvent(serverPlayer, resourcelocation)) {
                this.add(resourcelocation);
                this.addHighlight(resourcelocation);
                list.add(resourcelocation);
                CriteriaTriggers.RECIPE_UNLOCKED.trigger(serverPlayer, recipeholder);
                ++i;
            }
        }

        if (list.size() > 0) {
            this.sendRecipes(ClientboundRecipePacket.State.ADD, serverPlayer, list);
        }

        return i;
    }

    @Inject(method = "sendRecipes", cancellable = true, at = @At("HEAD"))
    public void banner$returnIfFail(ClientboundRecipePacket.State state, ServerPlayer player, List<ResourceLocation> recipesIn, CallbackInfo ci) {
        if (player.connection == null) {
            ci.cancel();
        }
    }
}
