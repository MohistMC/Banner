package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HarvestFarmland.class)
public abstract class MixinHarvestFarmland {

    @Shadow @Nullable private BlockPos aboveFarmlandPos;

    @WrapWithCondition(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$callFarmEvent(ServerLevel instance, BlockPos pos, boolean b, Entity entity) {
        return CraftEventFactory.callEntityChangeBlockEvent(entity, this.aboveFarmlandPos, Blocks.AIR.defaultBlockState());
    }

    @Inject(method = "tick*", at = @At("HEAD"))
    private void banner$getVillager(ServerLevel level, Villager owner, long gameTime, CallbackInfo ci) {
        BukkitSnapshotCaptures.captureEntityChangeBlock(owner);
    }
}
