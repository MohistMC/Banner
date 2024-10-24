package com.mohistmc.banner.mixin.world.entity.animal.frog;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.Tadpole;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Tadpole.class)
public abstract class MixinTadpole {

    // @formatter:off
    @Shadow protected abstract void setAge(int p_218711_);
    // @formatter:on

    @Inject(method = "ageUp()V", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/frog/Tadpole;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void banner$transform(CallbackInfo ci, ServerLevel serverLevel, Frog frog) {
        if (CraftEventFactory.callEntityTransformEvent((Tadpole) (Object) this, frog, org.bukkit.event.entity.EntityTransformEvent.TransformReason.METAMORPHOSIS).isCancelled()) {
            this.setAge(0); // Sets the age to 0 for avoid a loop if the event is canceled
            ci.cancel();
        } else {
             serverLevel.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.METAMORPHOSIS);
        }
    }
}
