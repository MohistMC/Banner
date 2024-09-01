package com.mohistmc.banner.mixin.world.damagesource;

import com.mohistmc.banner.injection.world.damagesource.InjectionCombatTracker;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CombatTracker.class)
public class MixinCombatTracker implements InjectionCombatTracker {

    @Shadow @Final private List<CombatEntry> entries;

    private Component banner$emptyComnent;

    @Inject(method = "getDeathMessage", cancellable = true, at = @At("HEAD"))
    private void banner$useOverride(CallbackInfoReturnable<Component> cir) {
        if (!this.entries.isEmpty()) {
            var entry = this.entries.get(this.entries.size() - 1);
            var deathMessage = entry.bridge$deathMessage();
            if (deathMessage != null) {
                cir.setReturnValue(deathMessage);
            }
        } else {
            if (this.banner$emptyComnent != null) {
                cir.setReturnValue(this.banner$emptyComnent);
            }
        }
        this.banner$emptyComnent = null;
    }

    @Override
    public void banner$setDeathMessage(Component component) {
        this.banner$emptyComnent = component;
        if (!this.entries.isEmpty()) {
            var entry = this.entries.get(this.entries.size() - 1);
            entry.banner$setDeathMessage(component);
        }
    }
}
