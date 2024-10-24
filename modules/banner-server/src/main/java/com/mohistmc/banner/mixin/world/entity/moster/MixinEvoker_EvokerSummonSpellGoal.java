package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.server.level.ServerLevel;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net.minecraft.world.entity.monster.Evoker$EvokerSummonSpellGoal")
public class MixinEvoker_EvokerSummonSpellGoal {

    @Inject(method = "performSpellCasting", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private void banner$reason(CallbackInfo ci, ServerLevel level) {
        level.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.SPELL);
    }
}
