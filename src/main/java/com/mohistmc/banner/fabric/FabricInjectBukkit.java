package com.mohistmc.banner.fabric;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.mohistmc.banner.BannerServer;
import com.mohistmc.dynamicenum.MohistDynamEnum;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.LevelStem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_19_R3.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v1_19_R3.potion.CraftPotionUtil;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class FabricInjectBukkit {

    public static final BiMap<ResourceKey<LevelStem>, World.Environment> DIM_MAP =
            HashBiMap.create(ImmutableMap.<ResourceKey<LevelStem>, World.Environment>builder()
                    .put(LevelStem.OVERWORLD, World.Environment.NORMAL)
                    .put(LevelStem.NETHER, World.Environment.NETHER)
                    .put(LevelStem.END, World.Environment.THE_END)
                    .build());

    public static void init() {
        addEnumMaterialInItems();
        addEnumMaterialsInBlocks();
        addEnumEnchantment();
        addEnumEffectAndPotion();
    }

    public static void addEnumMaterialInItems() {
        var registry = BuiltInRegistries.ITEM;
        for (Item item : registry) {
            ResourceLocation resourceLocation = registry.getKey(item);
            if (!isMINECRAFT(resourceLocation)) {
                // inject item materials into Bukkit for Fabric
                String materialName = normalizeName(resourceLocation.toString());
                int id = Item.getId(item);
                Material material = Material.addMaterial(materialName, id, false, resourceLocation.getNamespace());

                if (material != null) {
                    CraftMagicNumbers.ITEM_MATERIAL.put(item, material);
                    CraftMagicNumbers.MATERIAL_ITEM.put(material, item);
                    BannerServer.LOGGER.debug("Save-ITEM: " + material.name() + " - " + materialName);
                }
            }
        }
    }

    public static void addEnumMaterialsInBlocks() {
        var registry = BuiltInRegistries.BLOCK;
        for (Block block : registry) {
            ResourceLocation resourceLocation = registry.getKey(block);
            if (!isMINECRAFT(resourceLocation)) {
                // inject block materials into Bukkit for Fabric
                String materialName = normalizeName(resourceLocation.toString());
                int id = Item.getId(block.asItem());
                Material material = Material.addMaterial(materialName, id, true, resourceLocation.getNamespace());

                if (material != null) {
                    CraftMagicNumbers.BLOCK_MATERIAL.put(block, material);
                    CraftMagicNumbers.MATERIAL_BLOCK.put(material, block);
                    BannerServer.LOGGER.debug("Save-BLOCK:" + material.name() + " - " + materialName);
                }
            }
        }
    }

    public static void addEnumEnchantment() {
        // Enchantment
        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            org.bukkit.enchantments.Enchantment.registerEnchantment(new CraftEnchantment(enchantment));
        }
        org.bukkit.enchantments.Enchantment.stopAcceptingRegistrations();
    }

    public static void addEnumEffectAndPotion() {
        // Points
        for (MobEffect effect : BuiltInRegistries.MOB_EFFECT) {
            PotionEffectType pet = new CraftPotionEffectType(effect);
            PotionEffectType.registerPotionEffectType(pet);
        }
        PotionEffectType.stopAcceptingRegistrations();
        var registry = BuiltInRegistries.POTION;
        for (Potion potion : BuiltInRegistries.POTION) {
            ResourceLocation resourceLocation = registry.getKey(potion);
            if (CraftPotionUtil.toBukkit(resourceLocation.toString()).getType() == PotionType.UNCRAFTABLE && potion != Potions.EMPTY) {
                String name = normalizeName(resourceLocation.toString());
                MobEffectInstance effectInstance = potion.getEffects().isEmpty() ? null : potion.getEffects().get(0);
                PotionType potionType = MohistDynamEnum.addEnum0(PotionType.class, name, new Class[]{PotionEffectType.class, Boolean.TYPE, Boolean.TYPE}, effectInstance == null ? null : PotionEffectType.getById(MobEffect.getId(effectInstance.getEffect())), false, false);
                if (potionType != null) {
                    BannerServer.LOGGER.debug("Save-PotionType:" + name + " - " + potionType.name());
                }
            }
        }
    }

    public static String normalizeName(String name) {
        return name.toUpperCase(java.util.Locale.ENGLISH).replaceAll("(:|\\s)", "_").replaceAll("\\W", "");
    }

    public static boolean isMINECRAFT(ResourceLocation resourceLocation) {
        return resourceLocation.getNamespace().equals(NamespacedKey.MINECRAFT);
    }
}
