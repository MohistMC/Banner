package com.mohistmc.banner.api.mixin;

import com.mohistmc.banner.api.event.EntityJoinWorldEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class MixinServerLevel {

    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void onAddEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        EntityJoinWorldEvent banner$event = new EntityJoinWorldEvent(entity.getBukkitEntity(), entity.level().getWorld());
        if (banner$event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "addPlayer", at = @At("HEAD"), cancellable = true)
    private void onAddPlayer(ServerPlayer player, CallbackInfo ci) {
        EntityJoinWorldEvent banner$event = new EntityJoinWorldEvent(player.getBukkitEntity(), player.level().getWorld());
        if (banner$event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "addFreshEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addEntity(Lnet/minecraft/world/entity/Entity;)Z"), cancellable = true)
    private void onLoadEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        EntityJoinWorldEvent banner$event = new EntityJoinWorldEvent(entity.getBukkitEntity(), entity.level().getWorld());
        if (banner$event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }
}
