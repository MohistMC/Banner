package com.mohistmc.banner.mixin.core.advancement;

import com.mohistmc.banner.injection.advancements.InjectionAdvancement;
import net.minecraft.advancements.Advancement;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Advancement.class)
public class MixinAdvancement implements InjectionAdvancement {

    public final org.bukkit.advancement.Advancement bukkit =
            new org.bukkit.craftbukkit.v1_19_R3.advancement.CraftAdvancement(((Advancement) (Object) this)); // CraftBukkit

    @Override
    public org.bukkit.advancement.Advancement bridge$bukkit() {
        return bukkit;
    }
}