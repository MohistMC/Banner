package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.VillagerMakeLove;
import net.minecraft.world.entity.npc.Villager;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(VillagerMakeLove.class)
public class MixinVillagerMakeLove {

    @Redirect(method = "breed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;setAge(I)V",
            ordinal = 0))
    private void moveDownSetAge0(Villager instance, int i) {}

    @Redirect(method = "breed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;setAge(I)V", ordinal = 1))
    private void moveDownSetAge1(Villager instance, int i) {}

    @Inject(method = "breed", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$breadEvent(ServerLevel level, Villager parent, Villager partner,
                                  CallbackInfoReturnable<Optional<Villager>> cir,
                                  Villager villager) {
        // CraftBukkit start - call EntityBreedEvent
        if (CraftEventFactory.callEntityBreedEvent(villager, parent, partner, null, null, 0).isCancelled()) {
            cir.setReturnValue(Optional.empty());
        }
        // CraftBukkit end
        parent.setAge(6000);
        partner.setAge(6000);
        level.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.BREEDING);// CraftBukkit - added SpawnReason
    }
}
