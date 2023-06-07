package com.mohistmc.banner.fabric;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.api.ServerAPI;
import com.mohistmc.banner.entity.MohistModsEntity;
import com.mohistmc.dynamicenum.MohistDynamEnum;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.LevelStem;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R1.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_20_R1.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v1_20_R1.potion.CraftPotionUtil;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FabricInjectBukkit {

    public static final BiMap<ResourceKey<LevelStem>, World.Environment> DIM_MAP =
            HashBiMap.create(ImmutableMap.<ResourceKey<LevelStem>, World.Environment>builder()
                    .put(LevelStem.OVERWORLD, World.Environment.NORMAL)
                    .put(LevelStem.NETHER, World.Environment.NETHER)
                    .put(LevelStem.END, World.Environment.THE_END)
                    .build());
    public static Map<Villager.Profession, ResourceLocation> profession = new HashMap<>();
    public static Map<org.bukkit.attribute.Attribute, ResourceLocation> attributemap = new HashMap<>();

    public static void init() {
        addEnumMaterialInItems();
        addEnumMaterialsInBlocks();
        addEnumBiome();
        addEnumEnchantment();
        addEnumEffectAndPotion();
        addFluid();
        addEnumEntity();
        addEnumVillagerProfession();
        addEnumParticle();
    }

    public static void addEnumEnvironment() {
        int i = World.Environment.values().length;
        var registry = ServerAPI.getNMSServer().registryAccess().registryOrThrow(Registries.LEVEL_STEM);
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : registry.entrySet()) {
            ResourceKey<LevelStem> key = entry.getKey();
            World.Environment environment1 = DIM_MAP.get(key);
            if (environment1 == null) {
                String name = normalizeName(key.location().toString());
                int id = i - 1;
                environment1 = MohistDynamEnum.addEnum(World.Environment.class, name, new Class[]{Integer.TYPE}, new Object[]{id});
                DIM_MAP.put(key, environment1);
                BannerServer.LOGGER.debug("Registered fabric DimensionType as environment {}", environment1);
                i++;
            }
        }
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

    public static void addEnumParticle() {
        var registry = BuiltInRegistries.PARTICLE_TYPE;
        for (ParticleType particleType : BuiltInRegistries.PARTICLE_TYPE) {
            ResourceLocation resourceLocation = registry.getKey(particleType);
            String name = normalizeName(resourceLocation.toString());
            if (!resourceLocation.getNamespace().equals(NamespacedKey.MINECRAFT)) {
                Particle particle = MohistDynamEnum.addEnum0(Particle.class, name, new Class[0]);
                if (particle != null) {
                    org.bukkit.craftbukkit.v1_20_R1.CraftParticle.putParticles(particle, resourceLocation);
                    BannerServer.LOGGER.debug("Save-ParticleType:" + name + " - " + particle.name());
                }
            }
        }
    }

    public static void addEnumBiome() {
        List<String> map = new ArrayList<>();
        var registry = BuiltInRegistries.BIOME_SOURCE;
        for (Codec<? extends BiomeSource> biome : registry) {
            ResourceLocation resourceLocation = registry.getKey(biome);
            String biomeName = normalizeName(resourceLocation.toString());
            if (!isMINECRAFT(resourceLocation) && !map.contains(biomeName)) {
                map.add(biomeName);
                org.bukkit.block.Biome biomeCB = MohistDynamEnum.addEnum0(org.bukkit.block.Biome.class, biomeName, new Class[0]);
                BannerServer.LOGGER.debug("Save-BIOME:" + biomeCB.name() + " - " + biomeName);
            }
        }
        map.clear();
    }

    public static void addEnumEntity() {
        var registry = BuiltInRegistries.ENTITY_TYPE;
        for (net.minecraft.world.entity.EntityType<?> entity : registry) {
            ResourceLocation resourceLocation = registry.getKey(entity);
            if (!isMINECRAFT(resourceLocation)) {
                String entityType = normalizeName(resourceLocation.toString());
                int typeId = entityType.hashCode();
                EntityType bukkitType = MohistDynamEnum.addEnum0(EntityType.class, entityType, new Class[]{String.class, Class.class, Integer.TYPE, Boolean.TYPE}, entityType.toLowerCase(), MohistModsEntity.class, typeId, false);
                EntityType.NAME_MAP.put(entityType.toLowerCase(), bukkitType);
                EntityType.ID_MAP.put((short) typeId, bukkitType);
                ServerAPI.entityTypeMap.put(entity, entityType);
            }
        }
    }

    public static void addEnumVillagerProfession() {
        var registry = BuiltInRegistries.VILLAGER_PROFESSION;
        for (VillagerProfession villagerProfession : registry) {
            ResourceLocation resourceLocation = registry.getKey(villagerProfession);
            if (!isMINECRAFT(resourceLocation)) {
                String name = normalizeName(resourceLocation.toString());
                Villager.Profession vp = MohistDynamEnum.addEnum0(Villager.Profession.class, name, new Class[0]);
                profession.put(vp, resourceLocation);
                BannerServer.LOGGER.debug("Registered fabric VillagerProfession as Profession {}", vp.name());
            }
        }
    }

    public static void addFluid() {
        var registry = BuiltInRegistries.FLUID;
        for (net.minecraft.world.level.material.Fluid fluidType : registry) {
            ResourceLocation resourceLocation = registry.getKey(fluidType);
            String name = normalizeName(resourceLocation.getPath());
            if (!isMINECRAFT(resourceLocation)) {
                Fluid fluid = MohistDynamEnum.addEnum0(Fluid.class, name, new Class[0]);
                CraftMagicNumbers.FLUIDTYPE_FLUID.put(fluidType, fluid);
                BannerServer.LOGGER.debug("Registered fabric Fluid as Fluid(Bukkit) {}", fluid.name());
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
