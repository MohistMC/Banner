package com.mohistmc.banner.mixin.world.entity.frog;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.Tadpole;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Tadpole.class)
public abstract class MixinTadpole {

    @Shadow protected abstract void setAge(int i);

    @Inject(method = "ageUp()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/frog/Frog;fudgePositionAfterSizeChange(Lnet/minecraft/world/entity/EntityDimensions;)Z")
    )
    private void banner$transformerEvent(CallbackInfo ci, @Local Frog frog) {
        // CraftBukkit start
        if (CraftEventFactory.callEntityTransformEvent(((Tadpole) (Object) this), frog, org.bukkit.event.entity.EntityTransformEvent.TransformReason.METAMORPHOSIS).isCancelled()) {
            this.setAge(0); // Sets the age to 0 for avoid a loop if the event is canceled
        }
    }

    @Inject(method = "ageUp()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/frog/Tadpole;discard()V"))
    private void banner$pushRemoveReason(CallbackInfo ci, @Local Frog frog) {
        frog.pushRemoveCause(EntityRemoveEvent.Cause.TRANSFORMATION);
    }
}
