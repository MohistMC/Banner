package com.mohistmc.banner.mixin.world.entity.projectile;

import net.minecraft.world.entity.projectile.Fireball;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Fireball.class)
public class MixinFireball {

    // Banner TODO fixme
    /*
    @Inject(method = "readAdditionalSaveData", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Fireball;setItem(Lnet/minecraft/world/item/ItemStack;)V"))
    private void banner$nonNullItem(CompoundTag compoundTag, CallbackInfo ci) {
        if (stack.isEmpty()) ci.cancel();
    }*/
}
