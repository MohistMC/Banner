package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.PaintingVariant;
import org.bukkit.Art;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

public class CraftArt {

    public static Art minecraftToBukkit(PaintingVariant minecraft) {
        Preconditions.checkArgument(minecraft != null);

        net.minecraft.core.Registry<PaintingVariant> registry = CraftRegistry.getMinecraftRegistry(Registries.PAINTING_VARIANT);
        Art bukkit = Registry.ART.get(CraftNamespacedKey.fromMinecraft(registry.getResourceKey(minecraft).orElseThrow().location()));

        Preconditions.checkArgument(bukkit != null);

        return bukkit;
    }

    public static Art minecraftHolderToBukkit(Holder<PaintingVariant> minecraft) {
        return CraftArt.minecraftToBukkit(minecraft.value());
    }

    public static PaintingVariant bukkitToMinecraft(Art bukkit) {
        Preconditions.checkArgument(bukkit != null);

        return CraftRegistry.getMinecraftRegistry(Registries.PAINTING_VARIANT)
                .getOptional(CraftNamespacedKey.toMinecraft(bukkit.getKey())).orElseThrow();
    }

    public static Holder<PaintingVariant> bukkitToMinecraftHolder(Art bukkit) {
        Preconditions.checkArgument(bukkit != null);

        net.minecraft.core.Registry<PaintingVariant> registry = CraftRegistry.getMinecraftRegistry(Registries.PAINTING_VARIANT);

        if (registry.wrapAsHolder(CraftArt.bukkitToMinecraft(bukkit)) instanceof Holder.Reference<PaintingVariant> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own painting variant with out properly registering it.");
    }
}
