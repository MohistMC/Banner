package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import com.mohistmc.banner.fabric.BukkitRegistry;
import java.util.Locale;
import net.minecraft.core.registries.Registries;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.legacy.FieldRename;
import org.bukkit.craftbukkit.util.ApiVersion;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;

public class CraftEntityType {

    public static EntityType minecraftToBukkit(net.minecraft.world.entity.EntityType<?> minecraft) {
        Preconditions.checkArgument(minecraft != null);

        net.minecraft.core.Registry<net.minecraft.world.entity.EntityType<?>> registry = CraftRegistry.getMinecraftRegistry(Registries.ENTITY_TYPE);
        NamespacedKey key = CraftNamespacedKey.fromMinecraft(registry.getResourceKey(minecraft).orElseThrow().location());
        if (BukkitRegistry.entityTypeMap.containsKey(key)) {
            return BukkitRegistry.entityTypeMap.get(key);
        }
        EntityType bukkit = Registry.ENTITY_TYPE.get(key);

        Preconditions.checkArgument(bukkit != null);

        return bukkit;
    }

    public static net.minecraft.world.entity.EntityType<?> bukkitToMinecraft(EntityType bukkit) {
        Preconditions.checkArgument(bukkit != null);

        return CraftRegistry.getMinecraftRegistry(Registries.ENTITY_TYPE)
                .getOptional(CraftNamespacedKey.toMinecraft(bukkit.getKey())).orElseThrow();
    }

    public static String bukkitToString(EntityType bukkit) {
        Preconditions.checkArgument(bukkit != null);

        return bukkit.getKey().toString();
    }

    public static EntityType stringToBukkit(String string) {
        Preconditions.checkArgument(string != null);

        // We currently do not have any version-dependent remapping, so we can use current version
        // First convert from when only the names where saved
        string = FieldRename.convertEntityTypeName(ApiVersion.CURRENT, string);
        string = string.toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.fromString(string);

        // Now also convert from when keys where saved
        return CraftRegistry.get(Registry.ENTITY_TYPE, key, ApiVersion.CURRENT);
    }
}
