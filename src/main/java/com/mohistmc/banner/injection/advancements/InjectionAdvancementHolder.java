package com.mohistmc.banner.injection.advancements;

public interface InjectionAdvancementHolder {

    default org.bukkit.advancement.Advancement bridge$bukkit() {
        throw new IllegalStateException("Not implemented");
    }

    default org.bukkit.advancement.Advancement toBukkit() {
        throw new IllegalStateException("Not implemented");
    }
}
