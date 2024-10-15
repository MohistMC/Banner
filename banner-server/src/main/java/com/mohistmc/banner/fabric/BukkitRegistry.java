package com.mohistmc.banner.fabric;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.mohistmc.banner.BannerMod;
import com.mohistmc.banner.api.ServerAPI;
import com.mohistmc.banner.bukkit.MaterialHelper;
import com.mohistmc.banner.util.I18n;
import com.mohistmc.dynamicenum.MohistDynamEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.stats.StatType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.dimension.LevelStem;
import org.bukkit.Art;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.block.CraftBlockStates;
import org.bukkit.craftbukkit.block.CraftChest;
import org.bukkit.craftbukkit.block.CraftHangingSign;
import org.bukkit.craftbukkit.block.CraftSign;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.util.CraftSpawnCategory;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.entity.Villager;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings({"ConstantConditions", "deprecation"})
public class BukkitRegistry {

    public static final BiMap<ResourceKey<LevelStem>, World.Environment> environment =
            HashBiMap.create(ImmutableMap.<ResourceKey<LevelStem>, World.Environment>builder()
                    .put(LevelStem.OVERWORLD, World.Environment.NORMAL)
                    .put(LevelStem.NETHER, World.Environment.NETHER)
                    .put(LevelStem.END, World.Environment.THE_END)
                    .build());

    public static BiMap<World.Environment, ResourceKey<LevelStem>> environment0 =
            HashBiMap.create(ImmutableMap.<World.Environment, ResourceKey<LevelStem>>builder()
                    .put(World.Environment.NORMAL, LevelStem.OVERWORLD)
                    .put(World.Environment.NETHER, LevelStem.NETHER)
                    .put(World.Environment.THE_END, LevelStem.END)
                    .build());

    public static Map<ResourceKey<Registry<VillagerProfession>>, Villager.Profession> profession = new HashMap<>();
    public static Map<org.bukkit.attribute.Attribute, ResourceLocation> attributemap = new HashMap<>();
    public static Map<StatType<?>, Statistic> statisticMap = new HashMap<>();
    public static Map<net.minecraft.world.level.biome.Biome, Biome> biomeBiomeMap = new HashMap<>();
    public static Map<NamespacedKey, EntityType> entityTypeMap = new HashMap<>();
    public static Map<NamespacedKey, Particle> particleMap = new HashMap<>();

    public static void registerAll(DedicatedServer console) {
        loadItems();
        loadBlocks();
        loadPotions();
        loadEnchantments();
        loadEntities();
        //loadVillagerProfessions();
        loadBiomes(console);
        loadPoses();
        addPose();
        loadArts(console);
        loadStats();
        loadSpawnCategory();
        loadEndDragonPhase();
        loadCookingBookCategory();
        loadFluids();
        loadParticles();
    }

    public static void loadItems() {
        var registry = BuiltInRegistries.ITEM;
        var newTypes = new ArrayList<Material>();
        for (Item item : registry) {
            ResourceLocation resourceLocation = registry.getKey(item);
            if (isMods(resourceLocation)) {
                // inject item materials into Bukkit for Fabric
                String materialName = normalizeName(resourceLocation.toString());
                int id = Item.getId(item);
                Material material = MaterialHelper.addMaterial(materialName, id, item.getDefaultMaxStackSize(), false, true, resourceLocation);

                newTypes.add(material);

                CraftMagicNumbers.ITEM_MATERIAL.put(item, material);
                CraftMagicNumbers.MATERIAL_ITEM.put(material, item);
                BannerMod.LOGGER.debug("Save-ITEM: " + material.name() + " - " + material.key);
            }
        }
        BannerMod.LOGGER.info(I18n.as("registry.item"), newTypes.size());
    }

    public static void loadBlocks() {
        var registry = BuiltInRegistries.BLOCK;
        var newTypes = new ArrayList<Material>();

        for (Block block : registry) {
            ResourceLocation resourceLocation = registry.getKey(block);
            if (isMods(resourceLocation)) {
                // inject block materials into Bukkit for Fabric
                String materialName = normalizeName(resourceLocation.toString());
                int id = Item.getId(block.asItem());
                Item item = Item.byId(id);
                Material material = MaterialHelper.addMaterial(materialName, id, item.getDefaultMaxStackSize(), true, false, resourceLocation);
                newTypes.add(material);

                if (material != null) {
                    CraftMagicNumbers.BLOCK_MATERIAL.put(block, material);
                    CraftMagicNumbers.MATERIAL_BLOCK.put(material, block);
                    if (block.defaultBlockState().is(BlockTags.SIGNS)) {
                        CraftBlockStates.register(material, CraftSign.class, CraftSign::new, SignBlockEntity::new);
                    } else if (block.defaultBlockState().is(BlockTags.ALL_HANGING_SIGNS)) {
                        CraftBlockStates.register(material, CraftHangingSign.class, CraftHangingSign::new, HangingSignBlockEntity::new);
                    } else if (block instanceof SignBlock signBlock) {
                        BlockEntity blockEntity = signBlock.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
                        if (blockEntity instanceof HangingSignBlockEntity) {
                            CraftBlockStates.register(material, CraftHangingSign.class, CraftHangingSign::new, HangingSignBlockEntity::new);
                        } else if (blockEntity instanceof SignBlockEntity) {
                            CraftBlockStates.register(material, CraftSign.class, CraftSign::new, SignBlockEntity::new);
                        }
                    } else if (block instanceof ChestBlock chestBlock) {
                        BlockEntity blockEntity = chestBlock.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
                        if (blockEntity instanceof TrappedChestBlockEntity) {
                            CraftBlockStates.register(material, CraftChest.class, CraftChest::new, TrappedChestBlockEntity::new);
                        } else if (blockEntity instanceof ChestBlockEntity) {
                            CraftBlockStates.register(material, CraftChest.class, CraftChest::new, ChestBlockEntity::new);
                        }
                    }
                    BannerMod.LOGGER.debug("Registered {0} as block {1}" + material.name() + " - " + material.key);
                }
            }
        }
        BannerMod.LOGGER.info(I18n.as("registry.block"), newTypes.size());
    }

    private static void loadFluids() {
        var registry = BuiltInRegistries.FLUID;
        for (var fluidType : BuiltInRegistries.FLUID) {
            ResourceLocation resourceLocation = registry.getKey(fluidType);
            String name = normalizeName(resourceLocation.getPath());
            if (isMods(resourceLocation)) {
                org.bukkit.Fluid fluid = MohistDynamEnum.addEnum(org.bukkit.Fluid.class, name);
                BannerMod.LOGGER.debug("Registered Fluid as Fluid(Bukkit) {}", fluid.name());
            }
        }
    }

    private static void loadCookingBookCategory() {
        for (CookingBookCategory category : CookingBookCategory.values()) {
            try {
                CraftRecipe.getCategory(category);
            } catch (Exception e) {
                var name = category.name();
                var bukkit = MohistDynamEnum.addEnum(org.bukkit.inventory.recipe.CookingBookCategory.class, name);
                BannerMod.LOGGER.debug("Registered {} as cooking category {}", name, bukkit);
            }
        }
    }

    private static void loadEndDragonPhase() {
        var max = EnderDragonPhase.getCount();
        for (var id = EnderDragon.Phase.values().length; id < max; id++) {
            var name = "MOD_PHASE_" + id;
            var newPhase = MohistDynamEnum.addEnum(EnderDragon.Phase.class, name);
            BannerMod.LOGGER.debug("Registered {} as ender dragon phase {}", name, newPhase);
        }
    }

    private static void loadSpawnCategory() {
        for (var category : MobCategory.values()) {
            try {
                CraftSpawnCategory.toBukkit(category);
            } catch (Exception e) {
                var name = category.name();
                var spawnCategory = MohistDynamEnum.addEnum(SpawnCategory.class, name);
                spawnCategory.isMods = true;
                BannerMod.LOGGER.debug("Registered {} as spawn category {}", name, spawnCategory);
            }
        }
    }

    private static void loadStats() {
        var registry = BuiltInRegistries.STAT_TYPE;
        for (StatType<?> statType : registry) {
            ResourceLocation resourceLocation = registry.getKey(statType);
            String name = normalizeName(resourceLocation.getPath());
            if (isMods(resourceLocation)) {
                Statistic statistic = MohistDynamEnum.addEnum(Statistic.class, name);
                statisticMap.put(statType, statistic);
                BannerMod.LOGGER.debug("Registered mod StatType as Statistic(Bukkit) {}", statistic.name());
            }
        }
    }

    private static void loadPoses() {
        var newTypes = new ArrayList<org.bukkit.entity.Pose>();
        for (Pose pose : Pose.values()) {
            if (pose.ordinal() > 14) {
                org.bukkit.entity.Pose bukkit = MohistDynamEnum.addEnum(org.bukkit.entity.Pose.class, pose.name());
                newTypes.add(bukkit);
                BannerMod.LOGGER.debug("Registered mod Pose as Poses(Bukkit) {}", bukkit);
            }
        }
        BannerMod.LOGGER.info("Registered {} new Poses", newTypes.size());
    }

    private static void addPose() {
        for (Pose pose : Pose.values()) {
            if (pose.ordinal() > 14) {
                org.bukkit.entity.Pose bukkit = MohistDynamEnum.addEnum(org.bukkit.entity.Pose.class, pose.name());
                BannerMod.LOGGER.debug("Registered mod Pose as Pose(Bukkit) {}", bukkit);
            }
        }
    }

    private static void loadArts(DedicatedServer console) {
        int i = Art.values().length;
        var registry = console.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT);
        for (var entry : registry) {
            ResourceLocation resourceLocation = registry.getKey(entry);
            if (!resourceLocation.getNamespace().equals(NamespacedKey.MINECRAFT)) {
                String name = normalizeName(resourceLocation.toString());
                String lookupName = resourceLocation.getPath().toLowerCase(Locale.ROOT);
                int id = i - 1;
                Art art = MohistDynamEnum.addEnum(Art.class, name, List.of(Integer.TYPE, Integer.TYPE, Integer.TYPE), List.of(id, entry.width(), entry.height()));
                Art.BY_NAME.put(lookupName, art);
                Art.BY_ID.put(id, art);
                BannerMod.LOGGER.debug("Registered mod PaintingType as Art {}", art);
                i++;
            }
        }
    }

    public static void loadParticles() {
        var registry = BuiltInRegistries.PARTICLE_TYPE;
        for (ParticleType<?> particleType : BuiltInRegistries.PARTICLE_TYPE) {
            ResourceLocation resourceLocation = registry.getKey(particleType);
            String name = normalizeName(resourceLocation.toString());
            if (!resourceLocation.getNamespace().equals(NamespacedKey.MINECRAFT)) {
                NamespacedKey namespacedKey = CraftNamespacedKey.fromMinecraft(resourceLocation);
                Particle particle = MohistDynamEnum.addEnum(Particle.class, name, List.of(String.class), List.of(namespacedKey.toString()));
                if (particle != null) {
                    particle.key = namespacedKey;
                    particleMap.put(namespacedKey, particle);
                    BannerMod.LOGGER.debug("Save-ParticleType:" + name + " - " + particle.name());
                }
            }
        }
    }

    private static void loadBiomes(DedicatedServer console) {
        List<String> map = new ArrayList<>();
        var registry = console.registryAccess().registryOrThrow(Registries.BIOME);
        for (net.minecraft.world.level.biome.Biome biome : registry) {
            ResourceLocation resourceLocation = registry.getKey(biome);
            String biomeName = normalizeName(resourceLocation.toString());
            if (isMods(resourceLocation) && !map.contains(biomeName)) {
                map.add(biomeName);
                org.bukkit.block.Biome biomeCB = MohistDynamEnum.addEnum(org.bukkit.block.Biome.class, biomeName);
                biomeBiomeMap.put(biome, biomeCB);
                BannerMod.LOGGER.debug("Save-BIOME:" + biomeCB.name() + " - " + biomeName);
            }
        }
    }

    private static void loadVillagerProfessions() {
        var registry = BuiltInRegistries.VILLAGER_PROFESSION;
        for (VillagerProfession villagerProfession : registry) {
            ResourceLocation resourceLocation = registry.getKey(villagerProfession);
            if (isMods(resourceLocation)) {
                String name = normalizeName(resourceLocation.toString());
                Villager.Profession vp = MohistDynamEnum.addEnum(Villager.Profession.class, name);
                profession.put(ResourceKey.createRegistryKey(resourceLocation), vp);
                BannerMod.LOGGER.debug("Registered mod VillagerProfession as Profession {}", vp.name());
            }
        }
    }

    public static void registerEnvironments(Registry<LevelStem> registry) {
        int i = World.Environment.values().length;
        List<World.Environment> newTypes = new ArrayList<>();
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : registry.entrySet()) {
            ResourceKey<LevelStem> key = entry.getKey();
            World.Environment environment1 = environment.get(key);
            if (environment1 == null) {
                String name = normalizeName(key.location().toString());
                int id = i - 1;
                environment1 = MohistDynamEnum.addEnum(World.Environment.class, name, List.of(Integer.TYPE), List.of(id));
                environment.put(key, environment1);
                environment0.put(environment1, key);
                BannerMod.LOGGER.debug("Registered mod DimensionType as environment {}", environment1);
                i++;
            }
        }
    }

    private static void loadEntities() {
        var registry = BuiltInRegistries.ENTITY_TYPE;
        for (var entity : registry) {
            ResourceLocation resourceLocation = registry.getKey(entity);
            NamespacedKey key = CraftNamespacedKey.fromMinecraft(resourceLocation);
            String entityType = normalizeName(resourceLocation.toString());
            if (isMods(resourceLocation)) {
                int typeId = entityType.hashCode();
                EntityType bukkitType = MohistDynamEnum.addEnum(EntityType.class, entityType, List.of(String.class, Class.class, Integer.TYPE, Boolean.TYPE), List.of(entityType.toLowerCase(), Entity.class, typeId, false));
                bukkitType.key = key;
                entityTypeMap.put(key, bukkitType);
                EntityType.NAME_MAP.put(entityType.toLowerCase(), bukkitType);
                EntityType.ID_MAP.put((short) typeId, bukkitType);
                ServerAPI.entityTypeMap.put(entity, entityType);
                BannerMod.LOGGER.debug("Registered {} as entity {}", entityType, bukkitType);
            } else {
                ServerAPI.entityTypeMap.put(entity, normalizeName(resourceLocation.getPath()));
            }
        }
    }

    private static void loadEnchantments() {
        /*
        for (net.minecraft.world.item.enchantment.Enchantment enc : BuiltInRegistries) {
            try {
                Enchantment.getByKey(new CraftEnchantment(enc));
            } catch (Exception e) {
                BannerServer.LOGGER.error("Failed to register enchantment {}: {}", enc, e);
            }
        }
        Enchantment.stopAcceptingRegistrations();
        */
    }

    private static void loadPotions() {
        int typeId = PotionType.values().length;
        List<PotionType> newTypes = new ArrayList<>();
        for (var potion : BuiltInRegistries.POTION) {
            var location = BuiltInRegistries.POTION.getKey(potion);
            String name = normalizeName(location.toString());
            try {
                PotionType.valueOf(name);
            } catch (Exception e) {
                NamespacedKey namespacedKey = CraftNamespacedKey.fromMinecraft(location);
                PotionType potionType = MohistDynamEnum.addEnum(PotionType.class, name, List.of(String.class), List.of(namespacedKey.toString()));
                newTypes.add(potionType);
                BannerMod.LOGGER.debug("Registered {} as potion type {}", location, potionType);
            }
        }
    }

    public static String normalizeName(String name) {
        return name.replace(':', '_')
                .replaceAll("\\s+", "_")
                .replaceAll("\\W", "")
                .toUpperCase(Locale.ENGLISH);
    }

    public static boolean isMods(ResourceLocation resourceLocation) {
        return !resourceLocation.getNamespace().equals(NamespacedKey.MINECRAFT);
    }

    public static boolean isMods(NamespacedKey namespacedkey) {
        return !namespacedkey.getNamespace().equals(NamespacedKey.MINECRAFT);
    }
}