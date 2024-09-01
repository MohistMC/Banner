package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.world.entity.monster.Vex;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.monster.Vex$VexCopyOwnerTargetGoal")
public class MixinVex_CopyOwnerTargetGoal {

    @SuppressWarnings("target") @Shadow(aliases = {"field_7413"}, remap = false)
    private Vex outerThis;

    @Inject(method = "start", at = @At("HEAD"))
    private void arclight$reason(CallbackInfo ci) {
        outerThis.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true);
    }
}
