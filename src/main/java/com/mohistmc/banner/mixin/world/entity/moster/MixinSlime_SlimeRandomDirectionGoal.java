package com.mohistmc.banner.mixin.world.entity.moster;

import com.destroystokyo.paper.event.entity.SlimeChangeDirectionEvent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.Slime.SlimeRandomDirectionGoal")
public class MixinSlime_SlimeRandomDirectionGoal {

    @Shadow @Final private Slime slime;

    @Shadow private float chosenDegrees;

    /**
     * @author wdog5
     * @reason paper events
     */
    @Overwrite
    public boolean canUse() {
        return this.slime.getTarget() == null && (this.slime.onGround() || this.slime.isInWater() || this.slime.isInLava() || this.slime.hasEffect(MobEffects.LEVITATION)) && this.slime.getMoveControl() instanceof Slime.SlimeMoveControl && this.slime.canWander(); // Paper - add canWander
    }

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/RandomSource;nextInt(I)I",
            ordinal = 1, shift = At.Shift.AFTER), cancellable = true)
    private void banner$slimeEvent(CallbackInfo ci) {
        // Paper start
        SlimeChangeDirectionEvent event = new SlimeChangeDirectionEvent((org.bukkit.entity.Slime) this.slime.getBukkitEntity(), this.chosenDegrees);
        if (!event.callEvent()) ci.cancel();
        this.chosenDegrees = event.getNewYaw();
    }

}
