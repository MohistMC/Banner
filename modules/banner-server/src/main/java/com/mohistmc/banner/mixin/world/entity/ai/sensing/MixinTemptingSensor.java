package com.mohistmc.banner.mixin.world.entity.ai.sensing;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TemptingSensor.class)
public class MixinTemptingSensor {

    @Redirect(method = "doTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/PathfinderMob;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/Brain;setMemory(Lnet/minecraft/world/entity/ai/memory/MemoryModuleType;Ljava/lang/Object;)V"))
    private <U> void banner$entityTarget(Brain<?> instance, MemoryModuleType<U> memoryModuleType, U value, ServerLevel level, PathfinderMob mob) {
        var event = CraftEventFactory.callEntityTargetLivingEvent(mob, (Player) value, EntityTargetEvent.TargetReason.TEMPT);
        if (event.isCancelled()) {
            return;
        }
        if (event.getTarget() instanceof Player) {
            instance.setMemory(MemoryModuleType.TEMPTING_PLAYER, ((CraftHumanEntity) event.getTarget()).getHandle());
        } else {
            instance.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
        }
    }
}
