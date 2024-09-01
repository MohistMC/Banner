package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Pig.class)
public abstract class MixinPig extends Animal {

    protected MixinPig(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "thunderHit", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$pigZap(ServerLevel world, LightningBolt lightningBolt, CallbackInfo ci, ZombifiedPiglin piglin) {
        if (CraftEventFactory.callPigZapEvent((Pig) (Object) this, lightningBolt, piglin).isCancelled()) {
            ci.cancel();
        } else {
            this.level().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.LIGHTNING);
        }
    }
}
