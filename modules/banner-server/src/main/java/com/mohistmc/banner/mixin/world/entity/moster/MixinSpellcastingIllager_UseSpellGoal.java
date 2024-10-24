package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.world.entity.monster.SpellcasterIllager;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpellcasterIllager.SpellcasterUseSpellGoal.class)
public abstract class MixinSpellcastingIllager_UseSpellGoal {

    // @formatter:off
    @SuppressWarnings("target") @Shadow(aliases = {"field_7386"}, remap = false) private SpellcasterIllager outerThis;
    @Shadow(aliases = "method_7147") protected abstract SpellcasterIllager.IllagerSpell getSpell();
    // @formatter:on

    @Inject(method = "tick", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/SpellcasterIllager$SpellcasterUseSpellGoal;performSpellCasting()V"))
    private void banner$castSpell(CallbackInfo ci) {
        if (!CraftEventFactory.handleEntitySpellCastEvent(outerThis, this.getSpell())) {
            ci.cancel();
        }
    }
}
