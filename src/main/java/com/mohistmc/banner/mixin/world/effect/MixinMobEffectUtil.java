package com.mohistmc.banner.mixin.world.effect;

import com.mohistmc.banner.bukkit.BukkitCaptures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(MobEffectUtil.class)
public class MixinMobEffectUtil {

    private static AtomicReference<EntityPotionEffectEvent.Cause> banner$cause = new AtomicReference<>();

    /**
    @Inject(method = "addEffectToPlayersAround", locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
    private static void banner$pushCause(ServerLevel level, Entity source, Vec3 pos, double radius, MobEffectInstance effect, int durate, CallbackInfoReturnable<List<ServerPlayer>> cir, int duration, MobEffect mobEffect, List<ServerPlayer> list) {
        EntityPotionEffectEvent.Cause cause = banner$cause.get();
        cause = cause == null ? EntityPotionEffectEvent.Cause.UNKNOWN : BukkitCaptures.getEffectCause();
        if (cause != null) {
            for (ServerPlayer player : list) {
                player.pushEffectCause(cause);
            }
        }
    }*/
}
