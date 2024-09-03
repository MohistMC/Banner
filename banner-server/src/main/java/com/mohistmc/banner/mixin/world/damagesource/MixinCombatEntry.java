package com.mohistmc.banner.mixin.world.damagesource;

import com.mohistmc.banner.injection.world.damagesource.InjectionCombatEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.CombatEntry;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CombatEntry.class)
public class MixinCombatEntry implements InjectionCombatEntry {

    private Component banner$deathMessage;

    @Override
    public void banner$setDeathMessage(Component component) {
        this.banner$deathMessage = component;
    }

    @Override
    public Component bridge$deathMessage() {
        return this.banner$deathMessage;
    }
}
