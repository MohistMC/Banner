package com.mohistmc.banner.mixin.server.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.server.commands.EffectCommands;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(EffectCommands.class)
public class MixinEffectCommands {

    @Inject(method = "giveEffect", at = @At("HEAD"))
    private static void banner$addReason(CommandSourceStack source, Collection<? extends Entity> targets, Holder<MobEffect> effect, Integer seconds, int amplifier, boolean showParticles, CallbackInfoReturnable<Integer> cir) {
        for (Entity entity : targets) {
            if (entity instanceof LivingEntity) {
                 ((LivingEntity) entity).pushEffectCause(EntityPotionEffectEvent.Cause.COMMAND);
            }
        }
    }

    @Inject(method = "clearEffects", at = @At("HEAD"))
    private static void banner$removeAllReason(CommandSourceStack source, Collection<? extends Entity> targets, CallbackInfoReturnable<Integer> cir) {
        for (Entity entity : targets) {
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).pushEffectCause(EntityPotionEffectEvent.Cause.COMMAND);
            }
        }
    }

    @Inject(method = "clearEffect", at = @At("HEAD"))
    private static void banner$removeReason(CommandSourceStack source, Collection<? extends Entity> targets, Holder<MobEffect> effect, CallbackInfoReturnable<Integer> cir) {
        for (Entity entity : targets) {
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).pushEffectCause(EntityPotionEffectEvent.Cause.COMMAND);
            }
        }
    }
}
