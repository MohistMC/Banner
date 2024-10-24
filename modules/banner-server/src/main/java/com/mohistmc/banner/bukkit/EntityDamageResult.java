package com.mohistmc.banner.bukkit;

public record EntityDamageResult(
        boolean damageOverride,
        float originalDamage,
        float finalDamage,
        float damageOffset,
        float originalArmorDamage,
        float armorDamageOffset,
        boolean helmetHurtCancelled,
        boolean armorHurtCancelled,
        boolean blockingCancelled
) {
}
