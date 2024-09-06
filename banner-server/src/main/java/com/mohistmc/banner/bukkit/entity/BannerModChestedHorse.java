package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftChestedHorse;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.Horse;
import org.jetbrains.annotations.NotNull;

public class BannerModChestedHorse extends CraftChestedHorse {

    public BannerModChestedHorse(CraftServer server, AbstractChestedHorse entity) {
        super(server, entity);
    }

    @Override
    public Horse.@NotNull Variant getVariant() {
        return Horse.Variant.HORSE;
    }

    @Override
    public @NotNull EntityCategory getCategory() {
        return EntityCategory.NONE;
    }

    @Override
    public String toString() {
        return "BannerModChestedHorse{" + getType() + '}';
    }
}
