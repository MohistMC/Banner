package com.mohistmc.banner.mixin.world.entity.moster.piglin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractPiglin.class)
public class MixinAbstractPiglin {

    @Inject(method = "finishConversion", at = @At("HEAD"))
    private void banner$pushPiglinCause(ServerLevel serverLevel, CallbackInfo ci) {
        serverLevel.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.PIGLIN_ZOMBIFIED);
    }
}
