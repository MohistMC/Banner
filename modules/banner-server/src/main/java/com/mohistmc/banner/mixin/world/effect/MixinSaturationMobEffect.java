package com.mohistmc.banner.mixin.world.effect;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net.minecraft.world.effect.SaturationMobEffect")
public class MixinSaturationMobEffect {


    @Redirect(method = "applyEffectTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"))
    private void banner$foodLevelChange(FoodData instance, int amplifier, float f, @Local Player player) {
        int oldFoodLevel = player.getFoodData().foodLevel;
        org.bukkit.event.entity.FoodLevelChangeEvent event = CraftEventFactory.callFoodLevelChangeEvent(player, amplifier + 1 + oldFoodLevel);
        if (!event.isCancelled()) {
            player.getFoodData().eat(event.getFoodLevel() - oldFoodLevel, 1.0F);
        }

        ((CraftPlayer) player.getBukkitEntity()).sendHealthUpdate();
        // CraftBukkit end
    }
}
