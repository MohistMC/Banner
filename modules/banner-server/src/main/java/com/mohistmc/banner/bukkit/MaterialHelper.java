package com.mohistmc.banner.bukkit;

import com.mohistmc.dynamicenum.MohistDynamEnum;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.Material;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

public class MaterialHelper {

    public static Material addMaterial(String materialName, int id, int stack, boolean isBlock, boolean isItem, ResourceLocation resourceLocation) {
        if (isBlock) {
            Material material = Material.BY_NAME.get(materialName);
            if (material != null){
                material.isFabricBlock = true;
            }else {
                material = MohistDynamEnum.addEnum(Material.class, materialName, List.of(Integer.TYPE, Integer.TYPE, Boolean.TYPE, Boolean.TYPE), List.of(id, stack, isBlock, isItem));
            }
            Material.BY_NAME.put(materialName, material);
            material.key = CraftNamespacedKey.fromMinecraft(resourceLocation);
            return material;
        } else { // Forge Items
            Material material = MohistDynamEnum.addEnum(Material.class, materialName, List.of(Integer.TYPE, Integer.TYPE, Boolean.TYPE, Boolean.TYPE), List.of(id, stack, isBlock, isItem));
            Material.BY_NAME.put(materialName, material);
            material.key = CraftNamespacedKey.fromMinecraft(resourceLocation);
            return material;
        }
    }
}
