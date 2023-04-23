package com.mohistmc.banner.injection.advancements;

public interface InjectionAdvancement {

    default org.bukkit.advancement.Advancement bridge$bukkit() {
        return null;
    }
}
