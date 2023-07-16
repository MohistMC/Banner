package com.mohistmc.banner.mixin.world.entity.animal.axolotl;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Axolotl.class)
public abstract class MixinAxolotl extends Animal {

    protected MixinAxolotl(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow @Final private static int AXOLOTL_TOTAL_AIR_SUPPLY;

    @Inject(method = "getMaxAirSupply", cancellable = true, at = @At("RETURN"))
    private void banner$useBukkitMaxAir(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.bridge$maxAirTicks());
    }

    @Override
    public int getDefaultMaxAirSupply() {
        return AXOLOTL_TOTAL_AIR_SUPPLY;
    }

    @Inject(method = "applySupportingEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$cause(Player player, CallbackInfo ci) {
         player.pushEffectCause(EntityPotionEffectEvent.Cause.AXOLOTL);
    }
}
