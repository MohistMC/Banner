package com.mohistmc.banner.fabric;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.api.DynamicEnumHelper;
import com.mohistmc.banner.api.ServerAPI;
import com.mohistmc.banner.api.Unsafe;
import com.mohistmc.banner.entity.MohistModsEntity;
import com.mohistmc.banner.type.BannerEnchantment;
import com.mohistmc.banner.type.BannerPotionEffect;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.material.Fluid;
import org.bukkit.Art;
import org.bukkit.GameEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_20_R1.CraftStatistic;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v1_20_R1.potion.CraftPotionUtil;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftSpawnCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.entity.Villager;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings({"ConstantConditions", "deprecation"})
public class BukkitRegistry {

    public static final List<Class<?>> ENV_CTOR = ImmutableList.of(int.class);
    public static final Map<Integer, World.Environment> ENVIRONMENT_MAP =
            Unsafe.getStatic(World.Environment.class, "lookup");
    public static final BiMap<ResourceKey<LevelStem>, World.Environment> DIM_MAP =
            HashBiMap.create(ImmutableMap.<ResourceKey<LevelStem>, World.Environment>builder()
                    .put(LevelStem.OVERWORLD, World.Environment.NORMAL)
                    .put(LevelStem.NETHER, World.Environment.NETHER)
                    .put(LevelStem.END, World.Environment.THE_END)
                    .build());
    public static final Map<String, Art> ART_BY_NAME = Unsafe.getStatic(Art.class, "BY_NAME");
    public static final Map<Integer, Art> ART_BY_ID = Unsafe.getStatic(Art.class, "BY_ID");
    public static final BiMap<ResourceLocation, Statistic> STATS =
            HashBiMap.create(Unsafe.getStatic(CraftStatistic.class, "statistics"));
    public static final BiMap<Fluid, org.bukkit.Fluid> FLUIDTYPE_FLUID =
            Unsafe.getStatic(CraftMagicNumbers.class, "FLUIDTYPE_FLUID");
    public static Map<StatType<?>, Statistic> STATISTIC_MAP = new HashMap<>();
    public static Map<Villager.Profession, ResourceLocation> PROFESSION = new HashMap<>();
    public static Map<net.minecraft.world.level.biome.Biome, Biome> BIOME_MAP = new HashMap<>();

    public static void registerAll(DedicatedServer console) {
        loadItems();
        loadBlocks();
        loadPotions();
        loadEnchantments();
        loadEntities();
        loadVillagerProfessions();
        loadBiomes(console);
        loadArts();
        loadStats();
        loadSpawnCategory();
        loadEndDragonPhase();
        loadCookingBookCategory();
        loadFluids();
        loadGameEvents();

        try {
            for (var field : org.bukkit.Registry.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers())
                        && field.get(null) instanceof org.bukkit.Registry.SimpleRegistry<?> registry) {
                    registry.reloader.run();
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public static void loadItems() {
        var registry = BuiltInRegistries.ITEM;
        var newTypes = new ArrayList<Material>();
        for (Item item : registry) {
            ResourceLocation resourceLocation = registry.getKey(item);
            if (!isMINECRAFT(resourceLocation)) {
                // inject item materials into Bukkit for Fabric
                String materialName = normalizeName(resourceLocation.toString());
                int id = Item.getId(item);
                Material material = Material.addMaterial(materialName, id, false, resourceLocation.getNamespace());
                newTypes.add(material);

                if (material != null) {
                    CraftMagicNumbers.ITEM_MATERIAL.put(item, material);
                    CraftMagicNumbers.MATERIAL_ITEM.put(material, item);
                    BannerServer.LOGGER.debug("Registered {} as item {}" + material.name() + " - " + materialName);
                }
            }
        }
        BannerServer.LOGGER.info(BannerMCStart.I18N.get("registry.item"), newTypes.size());
    }

    public static void loadBlocks() {
        var registry = BuiltInRegistries.BLOCK;
        var newTypes = new ArrayList<Material>();

        for (Block block : registry) {
            ResourceLocation resourceLocation = registry.getKey(block);
            if (!isMINECRAFT(resourceLocation)) {
                // inject block materials into Bukkit for Fabric
                String materialName = normalizeName(resourceLocation.toString());
                int id = Item.getId(block.asItem());
                Material material = Material.addMaterial(materialName, id, true, resourceLocation.getNamespace());
                newTypes.add(material);

                if (material != null) {
                    CraftMagicNumbers.BLOCK_MATERIAL.put(block, material);
                    CraftMagicNumbers.MATERIAL_BLOCK.put(material, block);
                    BannerServer.LOGGER.debug("Registered {} as block {}" + material.name() + " - " + materialName);
                }
            }
        }
        BannerServer.LOGGER.info(BannerMCStart.I18N.get("registry.block"), newTypes.size());
    }

    private static void loadGameEvents() {
        try {
            var constructor = GameEvent.class.getDeclaredConstructor(NamespacedKey.class);
            constructor.setAccessible(true);
            var handle = Unsafe.lookup().unreflectConstructor(constructor);
            for (var gameEvent : BuiltInRegistries.GAME_EVENT) {
                var key = BuiltInRegistries.GAME_EVENT.getKey(gameEvent);
                var bukkit = GameEvent.getByKey(CraftNamespacedKey.fromMinecraft(key));
                if (bukkit == null) {
                    bukkit = (GameEvent) handle.invoke(CraftNamespacedKey.fromMinecraft(key));
                    BannerServer.LOGGER.debug("Registered {} as game event {}", key, bukkit);
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static void loadFluids() {
        var id = org.bukkit.Fluid.values().length;
        var newTypes = new ArrayList<org.bukkit.Fluid>();
        Field keyField = Arrays.stream(org.bukkit.Fluid.class.getDeclaredFields()).filter(it -> it.getName().equals("key")).findAny().orElse(null);
        long keyOffset = Unsafe.objectFieldOffset(keyField);
        for (var fluidType : BuiltInRegistries.FLUID) {
            if (!FLUIDTYPE_FLUID.containsKey(fluidType)) {
                var key = BuiltInRegistries.FLUID.getKey(fluidType);
                var name = normalizeName(key.toString());
                var bukkit = DynamicEnumHelper.makeEnum(org.bukkit.Fluid.class, name, id++, List.of(), List.of());
                Unsafe.putObject(bukkit, keyOffset, CraftNamespacedKey.fromMinecraft(key));
                newTypes.add(bukkit);
                FLUIDTYPE_FLUID.put(fluidType, bukkit);
                BannerServer.LOGGER.debug("Registered {} as fluid {}", key, bukkit);
            }
        }
        DynamicEnumHelper.addEnums(org.bukkit.Fluid.class, newTypes);
    }

    private static void loadCookingBookCategory() {
        var id = CookingBookCategory.values().length;
        var newTypes = new ArrayList<org.bukkit.inventory.recipe.CookingBookCategory>();
        for (CookingBookCategory category : CookingBookCategory.values()) {
            try {
                CraftRecipe.getCategory(category);
            } catch (Exception e) {
                var name = category.name();
                var bukkit = DynamicEnumHelper.makeEnum(org.bukkit.inventory.recipe.CookingBookCategory.class, name, id++, List.of(), List.of());
                newTypes.add(bukkit);
                BannerServer.LOGGER.debug("Registered {} as cooking category {}", name, bukkit);
            }
        }
        DynamicEnumHelper.addEnums(org.bukkit.inventory.recipe.CookingBookCategory.class, newTypes);
    }

    private static void loadEndDragonPhase() {
        var max = EnderDragonPhase.getCount();
        var newTypes = new ArrayList<EnderDragon.Phase>();
        for (var id = EnderDragon.Phase.values().length; id < max; id++) {
            var name = "MOD_PHASE_" + id;
            var newPhase = DynamicEnumHelper.makeEnum(EnderDragon.Phase.class, name, id, List.of(), List.of());
            newTypes.add(newPhase);
            BannerServer.LOGGER.debug("Registered {} as ender dragon phase {}", name, newPhase);
        }
        DynamicEnumHelper.addEnums(EnderDragon.Phase.class, newTypes);
    }

    private static void loadSpawnCategory() {
        var id = SpawnCategory.values().length;
        var newTypes = new ArrayList<SpawnCategory>();
        for (var category : MobCategory.values()) {
            try {
                CraftSpawnCategory.toBukkit(category);
            } catch (Exception e) {
                var name = category.name();
                var spawnCategory = DynamicEnumHelper.makeEnum(SpawnCategory.class, name, id++, List.of(), List.of());
                newTypes.add(spawnCategory);
                BannerServer.LOGGER.debug("Registered {} as spawn category {}", name, spawnCategory);
            }
        }
        DynamicEnumHelper.addEnums(SpawnCategory.class, newTypes);
    }

    private static void loadStats() {
        int i = Statistic.values().length;
        List<Statistic> newTypes = new ArrayList<>();
        Field key = Arrays.stream(Statistic.class.getDeclaredFields()).filter(it -> it.getName().equals("key")).findAny().orElse(null);
        long keyOffset = Unsafe.objectFieldOffset(key);
        for (StatType<?> statType : BuiltInRegistries.STAT_TYPE) {
            if (statType == Stats.CUSTOM) continue;
            var location = BuiltInRegistries.STAT_TYPE.getKey(statType);
            Statistic statistic = STATS.get(location);
            if (statistic == null) {
                String standardName = normalizeName(location.toString());
                Statistic.Type type;
                if (statType.getRegistry() == BuiltInRegistries.ENTITY_TYPE) {
                    type = Statistic.Type.ENTITY;
                } else if (statType.getRegistry() == BuiltInRegistries.BLOCK) {
                    type = Statistic.Type.BLOCK;
                } else if (statType.getRegistry() == BuiltInRegistries.ITEM) {
                    type = Statistic.Type.ITEM;
                } else {
                    type = Statistic.Type.UNTYPED;
                }
                statistic = DynamicEnumHelper.makeEnum(Statistic.class, standardName, i, ImmutableList.of(Statistic.Type.class), ImmutableList.of(type));
                Unsafe.putObject(statistic, keyOffset, location);
                newTypes.add(statistic);
                STATS.put(location, statistic);
                STATISTIC_MAP.put(statType, statistic);
                BannerServer.LOGGER.debug("Registered {} as stats {}", location, statistic);
                i++;
            }
        }
        for (ResourceLocation location : BuiltInRegistries.CUSTOM_STAT) {
            Statistic statistic = STATS.get(location);
            if (statistic == null) {
                String standardName = normalizeName(location.toString());
                statistic = DynamicEnumHelper.makeEnum(Statistic.class, standardName, i, ImmutableList.of(), ImmutableList.of());
                Unsafe.putObject(statistic, keyOffset, location);
                newTypes.add(statistic);
                STATS.put(location, statistic);
                BannerServer.LOGGER.debug("Registered {} as custom stats {}", location, statistic);
                i++;
            }
        }
        DynamicEnumHelper.addEnums(Statistic.class, newTypes);
        putStatic(CraftStatistic.class, "statistics", STATS);
    }

    private static void loadArts() {
        int i = Art.values().length;
        List<Art> newTypes = new ArrayList<>();
        Field key = Arrays.stream(Art.class.getDeclaredFields()).filter(it -> it.getName().equals("key")).findAny().orElse(null);
        long keyOffset = Unsafe.objectFieldOffset(key);
        for (var paintingType : BuiltInRegistries.PAINTING_VARIANT) {
            var location = BuiltInRegistries.PAINTING_VARIANT.getKey(paintingType);
            String lookupName = location.getPath().toLowerCase(Locale.ROOT);
            Art bukkit = Art.getByName(lookupName);
            if (bukkit == null) {
                String standardName = normalizeName(location.toString());
                bukkit = DynamicEnumHelper.makeEnum(Art.class, standardName, i, ImmutableList.of(int.class, int.class, int.class), ImmutableList.of(i, paintingType.getWidth(), paintingType.getHeight()));
                newTypes.add(bukkit);
                Unsafe.putObject(bukkit, keyOffset, CraftNamespacedKey.fromMinecraft(location));
                ART_BY_ID.put(i, bukkit);
                ART_BY_NAME.put(lookupName, bukkit);
                BannerServer.LOGGER.debug("Registered {} as art {}", location, bukkit);
                i++;
            }
        }
        DynamicEnumHelper.addEnums(Art.class, newTypes);
    }

    private static void loadBiomes(DedicatedServer console) {
        int i = Biome.values().length;
        List<Biome> newTypes = new ArrayList<>();
        Field key = Arrays.stream(Biome.class.getDeclaredFields()).filter(it -> it.getName().equals("key")).findAny().orElse(null);
        long keyOffset = Unsafe.objectFieldOffset(key);
        var registry = console.registryAccess().registryOrThrow(Registries.BIOME);
        for (net.minecraft.world.level.biome.Biome biome : registry) {
            var location = registry.getKey(biome);
            if (!isMINECRAFT(location)) {
                String name = normalizeName(location.toString());
                Biome bukkit;
                try {
                    bukkit = Biome.valueOf(name);
                } catch (Throwable t) {
                    bukkit = null;
                }
                if (bukkit == null) {
                    bukkit = DynamicEnumHelper.makeEnum(Biome.class, name, i++, ImmutableList.of(), ImmutableList.of());
                    newTypes.add(bukkit);
                    Unsafe.putObject(bukkit, keyOffset, CraftNamespacedKey.fromMinecraft(location));
                    BannerServer.LOGGER.debug("Registered {} as biome {}", location, bukkit);
                }
            }
        }
        DynamicEnumHelper.addEnums(Biome.class, newTypes);
        BannerServer.LOGGER.info(BannerMCStart.I18N.get("registry.biome"), newTypes.size());
    }

    private static void loadVillagerProfessions() {
        int i = Villager.Profession.values().length;
        List<Villager.Profession> newTypes = new ArrayList<>();
        Field key = Arrays.stream(Villager.Profession.class.getDeclaredFields()).filter(it -> it.getName().equals("key")).findAny().orElse(null);
        long keyOffset = Unsafe.objectFieldOffset(key);
        for (VillagerProfession villagerProfession : BuiltInRegistries.VILLAGER_PROFESSION) {
            var location = BuiltInRegistries.VILLAGER_PROFESSION.getKey(villagerProfession);
            if (!isMINECRAFT(location)) {
                String name = normalizeName(location.toString());
                Villager.Profession profession;
                try {
                    profession = Villager.Profession.valueOf(name);
                } catch (Throwable t) {
                    profession = null;
                }
                if (profession == null) {
                    profession = DynamicEnumHelper.makeEnum(Villager.Profession.class, name, i++, ImmutableList.of(), ImmutableList.of());
                    newTypes.add(profession);
                    Unsafe.putObject(profession, keyOffset, CraftNamespacedKey.fromMinecraft(location));
                    BannerServer.LOGGER.debug("Registered {} as villager profession {}", location, profession);
                }
                PROFESSION.put(profession, location);
            }
        }
        DynamicEnumHelper.addEnums(Villager.Profession.class, newTypes);
        BannerServer.LOGGER.info(BannerMCStart.I18N.get("registry.villager-profession"), newTypes.size());
    }

    public static void registerEnvironments(Registry<LevelStem> registry) {
        int i = World.Environment.values().length;
        List<World.Environment> newTypes = new ArrayList<>();
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : registry.entrySet()) {
            ResourceKey<LevelStem> key = entry.getKey();
            World.Environment environment = DIM_MAP.get(key);
            if (environment == null) {
                String name = normalizeName(key.location().toString());
                environment = DynamicEnumHelper.makeEnum(World.Environment.class, name, i, ENV_CTOR, ImmutableList.of(i - 1));
                newTypes.add(environment);
                ENVIRONMENT_MAP.put(i - 1, environment);
                DIM_MAP.put(key, environment);
                BannerServer.LOGGER.debug("Registered {} as environment {}", key.location(), environment);
                i++;
            }
        }
        DynamicEnumHelper.addEnums(World.Environment.class, newTypes);
        BannerServer.LOGGER.info(BannerMCStart.I18N.get("registry.environment"), newTypes.size());
    }

    private static void loadEntities() {
        int ordinal = EntityType.values().length;
        List<EntityType> values = new ArrayList<>();
        for (var entry : BuiltInRegistries.ENTITY_TYPE) {
            var location = BuiltInRegistries.ENTITY_TYPE.getKey(entry);
            var enumName = normalizeName(location.toString());
            ServerAPI.entityTypeMap.put(entry, enumName);
            if (isMINECRAFT(location)) {
                continue;
            }
            int typeId = enumName.hashCode();
            try {
                var bukkitType = DynamicEnumHelper.makeEnum(EntityType.class, enumName, ordinal,
                        List.of(String.class, Class.class, Integer.TYPE, Boolean.TYPE),
                        List.of(enumName.toLowerCase(), MohistModsEntity.class, typeId, false));
                EntityType.NAME_MAP.put(enumName.toLowerCase(), bukkitType);
                EntityType.ID_MAP.put((short) typeId, bukkitType);
                ordinal++;
                values.add(bukkitType);
                BannerServer.LOGGER.debug("Registered {} as entity {}" + enumName);
            } catch (Throwable e) {
                BannerServer.LOGGER.error("Not found {} in {}" + enumName + ". " + e.getMessage());
            }
        }
        DynamicEnumHelper.addEnums(EntityType.class, values);
        BannerServer.LOGGER.info(BannerMCStart.I18N.get("registry.entity-type"), values.size());
    }

    private static void loadEnchantments() {
        int origin = Enchantment.values().length;
        int size = BuiltInRegistries.ENCHANTMENT.size();
        putBool(Enchantment.class, "acceptingNew", true);
        for (net.minecraft.world.item.enchantment.Enchantment enc : BuiltInRegistries.ENCHANTMENT) {
            try {
                var location = BuiltInRegistries.ENCHANTMENT.getKey(enc);
                String name = normalizeName(location.toString());
                BannerEnchantment enchantment = new BannerEnchantment(enc, name);
                Enchantment.registerEnchantment(enchantment);
                BannerServer.LOGGER.debug("Registered {} as enchantment {}", location, enchantment);
            } catch (Exception e) {
                BannerServer.LOGGER.error("Failed to register enchantment {}: {}", enc, e);
            }
        }
        Enchantment.stopAcceptingRegistrations();
        BannerServer.LOGGER.info(BannerMCStart.I18N.get("registry.enchantment"), size - origin);
    }

    private static void loadPotions() {
        int origin = PotionEffectType.values().length;
        int size = BuiltInRegistries.MOB_EFFECT.size();
        int maxId = BuiltInRegistries.MOB_EFFECT.stream().mapToInt(MobEffect::getId).max().orElse(0);
        PotionEffectType[] types = new PotionEffectType[maxId + 1];
        putStatic(PotionEffectType.class, "byId", types);
        putBool(PotionEffectType.class, "acceptingNew", true);
        for (MobEffect eff : BuiltInRegistries.MOB_EFFECT) {
            try {
                var location = BuiltInRegistries.MOB_EFFECT.getKey(eff);
                String name = normalizeName(location.toString());
                BannerPotionEffect effect = new BannerPotionEffect(eff, name);
                PotionEffectType.registerPotionEffectType(effect);
                BannerServer.LOGGER.debug("Registered {} as potion {}", location, effect);
            } catch (Exception e) {
                BannerServer.LOGGER.error("Failed to register potion type {}: {}", eff, e);
            }
        }
        PotionEffectType.stopAcceptingRegistrations();
        BannerServer.LOGGER.info(BannerMCStart.I18N.get("registry.potion"), size - origin);
        int typeId = PotionType.values().length;
        List<PotionType> newTypes = new ArrayList<>();
        BiMap<PotionType, String> map = HashBiMap.create(Unsafe.getStatic(CraftPotionUtil.class, "regular"));
        putStatic(CraftPotionUtil.class, "regular", map);
        for (var potion : BuiltInRegistries.POTION) {
            var location = BuiltInRegistries.POTION.getKey(potion);
            if (!isMINECRAFT(location)) {
                if (CraftPotionUtil.toBukkit(location.toString()).getType() == PotionType.UNCRAFTABLE && potion != Potions.EMPTY) {
                    String name = normalizeName(location.toString());
                    MobEffectInstance effectInstance = potion.getEffects().isEmpty() ? null : potion.getEffects().get(0);
                    PotionType potionType = DynamicEnumHelper.makeEnum(PotionType.class, name, typeId++,
                            Arrays.asList(PotionEffectType.class, boolean.class, boolean.class),
                            Arrays.asList(effectInstance == null ? null : PotionEffectType.getById(MobEffect.getId(effectInstance.getEffect())), false, false));
                    newTypes.add(potionType);
                    map.put(potionType, location.toString());
                    BannerServer.LOGGER.debug("Registered {} as potion type {}", location, potionType);
                }
            }
        }
        DynamicEnumHelper.addEnums(PotionType.class, newTypes);
    }

    private static void putStatic(Class<?> cl, String name, Object o) {
        try {
            Unsafe.ensureClassInitialized(cl);
            Field field = cl.getDeclaredField(name);
            Object materialByNameBase = Unsafe.staticFieldBase(field);
            long materialByNameOffset = Unsafe.staticFieldOffset(field);
            Unsafe.putObject(materialByNameBase, materialByNameOffset, o);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void putBool(Class<?> cl, String name, boolean b) {
        try {
            Unsafe.ensureClassInitialized(cl);
            Field field = cl.getDeclaredField(name);
            Object materialByNameBase = Unsafe.staticFieldBase(field);
            long materialByNameOffset = Unsafe.staticFieldOffset(field);
            Unsafe.putBoolean(materialByNameBase, materialByNameOffset, b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String normalizeName(String name) {
        return name.toUpperCase(java.util.Locale.ENGLISH).replaceAll("(:|\\s)", "_")
                .replaceAll("\\W", "");
    }

    public static boolean isMINECRAFT(ResourceLocation resourceLocation) {
        return resourceLocation.getNamespace().equals(NamespacedKey.MINECRAFT);
    }
}