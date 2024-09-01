package com.mohistmc.banner.mixin.advancement;

import com.mohistmc.banner.injection.advancements.InjectionAdvancementHolder;
import net.minecraft.advancements.AdvancementHolder;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.advancement.CraftAdvancement;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AdvancementHolder.class)
public class MixinAdvancementHolder implements InjectionAdvancementHolder {

    public final org.bukkit.advancement.Advancement bukkit =
            new CraftAdvancement(((AdvancementHolder) (Object) this)); // CraftBukkit

    @Override
    public org.bukkit.advancement.Advancement bridge$bukkit() {
        return bukkit;
    }

    @Override
    public Advancement toBukkit() {
        return bridge$bukkit();
    }
}