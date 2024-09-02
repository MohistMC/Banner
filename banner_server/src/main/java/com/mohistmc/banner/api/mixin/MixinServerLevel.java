package com.mohistmc.banner.api.mixin;

import com.mohistmc.banner.api.event.EntityJoinWorldEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.bukkit.event.EventException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class MixinServerLevel {

    @Inject(method = "addEntity", at = @At("HEAD"))
    private void onAddEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        new EntityJoinWorldEvent(entity.getBukkitEntity(), entity.level().getWorld()).callEvent();
    }

    @Inject(method = "addPlayer", at = @At("HEAD"))
    private void onAddPlayer(ServerPlayer player, CallbackInfo ci) throws EventException {
        new EntityJoinWorldEvent(player.getBukkitEntity(), player.level().getWorld()).callEvent();
    }

    @Inject(method = "addFreshEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addEntity(Lnet/minecraft/world/entity/Entity;)Z"), cancellable = true)
    private void onLoadEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        new EntityJoinWorldEvent(entity.getBukkitEntity(), entity.level().getWorld()).callEvent();
    }
}
