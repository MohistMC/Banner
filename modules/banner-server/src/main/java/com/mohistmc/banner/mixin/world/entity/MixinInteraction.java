package com.mohistmc.banner.mixin.world.entity;

import net.minecraft.advancements.critereon.PlayerHurtEntityTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Interaction.class)
public class MixinInteraction {

    private double banner$finalDamage;

    @Inject(method = "skipAttackInteraction", at = @At("HEAD"), cancellable = true)
    private void fireEntityDamageEvent(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player banner$player) {
            DamageSource source = banner$player.damageSources().playerAttack(banner$player);
            EntityDamageEvent event = CraftEventFactory.callNonLivingEntityDamageEvent(((Interaction) (Object) this), source, 1.0F, false);
            if (event.isCancelled()) {
                cir.setReturnValue(true);
            } else {
                banner$finalDamage = event.getFinalDamage();
            }
        }
    }

    @Redirect(method = "skipAttackInteraction", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/advancements/critereon/PlayerHurtEntityTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;FFZ)V"))
    private void banner$setDamage(PlayerHurtEntityTrigger instance, ServerPlayer player, Entity entity, DamageSource source, float amountDealt, float amountTaken, boolean blocked) {
        instance.trigger(player, entity, source, (float) banner$finalDamage, amountTaken, blocked);
    }
}
