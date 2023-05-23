package com.mohistmc.banner.mixin.world.effect;

import com.mohistmc.banner.bukkit.BukkitCaptures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(MobEffectUtil.class)
public class MixinMobEffectUtil {

    @Inject(method = "addEffectToPlayersAround", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
    private static void banner$pushCause(ServerLevel level, Entity source, Vec3 pos, double radius, MobEffectInstance effect, int duration1, CallbackInfoReturnable<List<ServerPlayer>> cir, int duration, MobEffect mobEffect, List<ServerPlayer> list) {
        var cause = BukkitCaptures.getEffectCause();
        if (cause != null) {
            for (ServerPlayer player : list) {
                 player.pushEffectCause(cause);
            }
        }
    }
}
