package com.mohistmc.banner.mixin.world.entity.ai.village;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.VillageSiege;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillageSiege.class)
public class MixinVillageSiege {

    @Inject(method = "trySpawn", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Zombie;moveTo(DDDFF)V", shift = At.Shift.AFTER))
    private void banner$pushAddReason(ServerLevel level, CallbackInfo ci) {
        level.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION);
    }

}
