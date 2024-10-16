package com.mohistmc.banner.mixin.world.entity;

import net.minecraft.advancements.critereon.PlayerHurtEntityTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.event.entity.EntityDamageEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Interaction.class)
public class MixinInteraction {

    @Unique
    private double banner$finalDamage;
    private DamageSource banner$source;

    @Inject(method = "skipAttackInteraction", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/entity/Interaction;attack:Lnet/minecraft/world/entity/Interaction$PlayerAction;"))
    private void fireEntityDamageEvent(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        DamageSource source = entity.damageSources().playerAttack((Player) entity);
        EntityDamageEvent event = CraftEventFactory.callNonLivingEntityDamageEvent(((Interaction) (Object) this), source, 1.0F, false);
        if (event.isCancelled()) {
            cir.setReturnValue(true);
        } else {
            banner$finalDamage = event.getFinalDamage();
            banner$source = source;
        }
    }

    @Redirect(method = "skipAttackInteraction", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/advancements/critereon/PlayerHurtEntityTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;FFZ)V"))
    private void banner$setDamage(PlayerHurtEntityTrigger instance, ServerPlayer player, Entity entity, DamageSource source, float amountDealt, float amountTaken, boolean blocked) {
        instance.trigger(player, entity, banner$source, (float) banner$finalDamage, amountTaken, blocked);
    }
}
