package com.mohistmc.banner.mixin.stats;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

@Mixin(ServerRecipeBook.class)
public class MixinServerRecipeBook {

    @Inject(method = "addRecipes",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/stats/ServerRecipeBook;add(Lnet/minecraft/resources/ResourceLocation;)V"), cancellable = true)
    private void banner$callRecipeEvent(Collection<RecipeHolder<?>> collection, ServerPlayer serverPlayer, CallbackInfoReturnable<Integer> cir, @Local ResourceLocation resourceLocation) {
        if (!CraftEventFactory.handlePlayerRecipeListUpdateEvent(serverPlayer, resourceLocation)) {
            cir.cancel();
        }
    }

    @Inject(method = "sendRecipes", at = @At("HEAD"), cancellable = true)
    private void banner$checkCon(ClientboundRecipePacket.State state, ServerPlayer serverPlayer, List<ResourceLocation> list, CallbackInfo ci) {
        if (serverPlayer.connection == null) {
            ci.cancel();
        }
        // SPIGOT-4478 during PlayerLoginEvent
    }
}
