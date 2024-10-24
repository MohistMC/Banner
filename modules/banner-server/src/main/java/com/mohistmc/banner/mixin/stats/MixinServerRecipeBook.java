package com.mohistmc.banner.mixin.stats;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.Collection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerRecipeBook.class)
public class MixinServerRecipeBook {

    @Inject(method = "addRecipes",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/stats/ServerRecipeBook;add(Lnet/minecraft/resources/ResourceKey;)V"), cancellable = true)
    private void banner$callRecipeEvent(Collection<RecipeHolder<?>> collection, ServerPlayer serverPlayer, CallbackInfoReturnable<Integer> cir, @Local ResourceLocation resourceLocation) {
        if (!CraftEventFactory.handlePlayerRecipeListUpdateEvent(serverPlayer, resourceLocation)) {
            cir.cancel();
        }
    }
}
