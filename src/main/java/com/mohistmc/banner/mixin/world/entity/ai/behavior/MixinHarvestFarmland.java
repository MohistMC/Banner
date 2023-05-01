package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import com.mohistmc.banner.bukkit.BukkitCaptures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.npc.Villager;

@Mixin(HarvestFarmland.class)
public class MixinHarvestFarmland {

    @Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private void on(ServerLevel worldIn, Villager owner, long gameTime, CallbackInfo ci) {
        BukkitCaptures.captureEntityChangeBlock(owner);
    }
}
