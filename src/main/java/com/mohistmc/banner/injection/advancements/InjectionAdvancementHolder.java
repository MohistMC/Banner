package com.mohistmc.banner.injection.advancements;

public interface InjectionAdvancementHolder {

    default org.bukkit.advancement.Advancement bridge$bukkit() {
        return null;
    }
}
