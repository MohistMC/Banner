package com.mohistmc.banner.mixin.world.effect;

import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.world.effect.MobEffectUtil;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;

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
