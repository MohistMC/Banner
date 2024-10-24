package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import net.minecraft.core.registries.Registries;
import org.bukkit.Fluid;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

public class CraftFluid {

    public static Fluid minecraftToBukkit(net.minecraft.world.level.material.Fluid minecraft) {
        Preconditions.checkArgument(minecraft != null);

        net.minecraft.core.Registry<net.minecraft.world.level.material.Fluid> registry = CraftRegistry.getMinecraftRegistry(Registries.FLUID);
        Fluid bukkit = Registry.FLUID.get(CraftNamespacedKey.fromMinecraft(registry.getResourceKey(minecraft).orElseThrow().location()));

        Preconditions.checkArgument(bukkit != null);

        return bukkit;
    }

    public static net.minecraft.world.level.material.Fluid bukkitToMinecraft(Fluid bukkit) {
        Preconditions.checkArgument(bukkit != null);

        return CraftRegistry.getMinecraftRegistry(Registries.FLUID)
                .getOptional(CraftNamespacedKey.toMinecraft(bukkit.getKey())).orElseThrow();
    }
}
