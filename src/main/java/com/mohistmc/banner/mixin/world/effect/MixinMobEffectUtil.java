package com.mohistmc.banner.mixin.world.effect;

import net.minecraft.world.effect.MobEffectUtil;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MobEffectUtil.class)
public class MixinMobEffectUtil {

    /**
    @Inject(method = "addEffectToPlayersAround", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
    private static void banner$pushCause(ServerLevel level, Entity source, Vec3 pos, double radius, MobEffectInstance effect, int durate, CallbackInfoReturnable<List<ServerPlayer>> cir, int duration, MobEffect mobEffect, List<ServerPlayer> list) {
        var cause = BukkitCaptures.getEffectCause();
        if (cause != null) {
            for (ServerPlayer player : list) {
                 player.pushEffectCause(cause);
            }
        }
    }*/
}
