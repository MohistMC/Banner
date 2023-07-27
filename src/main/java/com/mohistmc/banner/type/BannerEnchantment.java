package com.mohistmc.banner.type;

import net.minecraft.world.item.enchantment.Enchantment;
import org.bukkit.craftbukkit.v1_20_R1.enchantments.CraftEnchantment;
import org.jetbrains.annotations.NotNull;

public class BannerEnchantment extends CraftEnchantment {

    private final String name;

    public BannerEnchantment(Enchantment target, String name) {
        super(target);
        this.name = name;
    }

    @Override
    public @NotNull String getName() {
        String name = super.getName();
        if (name.startsWith("UNKNOWN_ENCHANT_")) {
            return this.name;
        } else {
            return name;
        }
    }
}