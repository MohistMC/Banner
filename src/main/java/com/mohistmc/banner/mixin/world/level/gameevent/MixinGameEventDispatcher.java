package com.mohistmc.banner.mixin.world.level.gameevent;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftNamespacedKey;
import org.bukkit.event.world.GenericGameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(GameEventDispatcher.class)
public class MixinGameEventDispatcher {

    @Shadow @Final private ServerLevel level;
    private transient int banner$newRadius;

    @Inject(method = "post", cancellable = true, at = @At("HEAD"))
    private void banner$gameEvent(GameEvent gameEvent, Vec3 vec3, GameEvent.Context context, CallbackInfo ci) {
        var entity = context.sourceEntity();
        var i = gameEvent.getNotificationRadius();
        GenericGameEvent event = new GenericGameEvent(Objects.requireNonNull(org.bukkit.GameEvent.getByKey(CraftNamespacedKey.fromMinecraft(BuiltInRegistries.GAME_EVENT.getKey(gameEvent)))),
                new Location(this.level.getWorld(), vec3.x(), vec3.y(), vec3.z()), (entity == null) ? null : entity.getBukkitEntity(), i, !Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        } else {
            banner$newRadius = event.getRadius();
        }
    }

    @Redirect(method = "post", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/gameevent/GameEvent;getNotificationRadius()I"))
    private int banner$applyRadius(GameEvent instance) {
        return banner$newRadius;
    }
}
