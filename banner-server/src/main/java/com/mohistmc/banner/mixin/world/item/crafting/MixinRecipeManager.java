package com.mohistmc.banner.mixin.world.item.crafting;

import com.google.gson.JsonObject;
import com.mohistmc.banner.injection.world.item.crafting.InjectionRecipeManager;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// Banner TODO fix patch
@Mixin(RecipeManager.class)
public abstract class MixinRecipeManager implements InjectionRecipeManager {
    @Shadow private boolean hasErrors;

    @Shadow @Final private static Logger LOGGER;
    @Shadow private Map<ResourceLocation, RecipeHolder<?>> byName;

    @Shadow
    protected static RecipeHolder<?> fromJson(ResourceLocation resourceLocation, JsonObject jsonObject, HolderLookup.Provider provider) {
        return null;
    }

    @Shadow @Final private HolderLookup.Provider registries;
    /**
    public Map<RecipeType<?>, Object2ObjectLinkedOpenHashMap<ResourceLocation, RecipeHolder<?>>> recipesCB = ImmutableMap.of(); // CraftBukkit  // Mohist use obf name


    // Banner - fix mixin
    protected Map<RecipeType<?>, Object2ObjectLinkedOpenHashMap<ResourceLocation, Recipe<?>>> map1;
    /**
     * @author wdog5
     * @reason bukkit current recipe
     */
    /**
    @Overwrite
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        this.hasErrors = false;

        // CraftBukkit start - SPIGOT-5667 make sure all types are populated and mutable
        map1 = Maps.newHashMap();
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            map1.put(recipeType, new Object2ObjectLinkedOpenHashMap<>());
        }
        // CraftBukkit end

        Map<RecipeType<?>, ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>>> map = Maps.newHashMap();
        ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> builder = ImmutableMap.builder();
        RegistryOps<JsonElement> registryOps = this.registries.createSerializationContext(JsonOps.INSTANCE);

        Iterator var6 = pObject.entrySet().iterator();

        while(var6.hasNext()) {
            Map.Entry<ResourceLocation, JsonElement> entry = (Map.Entry)var6.next();
            ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();

            try {
                Recipe<?> recipe = (Recipe)Recipe.CODEC.parse(registryOps, (JsonElement)entry.getValue()).getOrThrow(JsonParseException::new);
                RecipeHolder<?> recipeHolder = new RecipeHolder(resourceLocation, recipe);                map.computeIfAbsent(recipe.value().getType(), (p_44075_) -> {
                    return ImmutableMap.builder();
                }).put(resourceLocation, recipe);

                // CraftBukkit start
                (map1.computeIfAbsent(recipe.value().getType(), (recipes) -> {
                    return new Object2ObjectLinkedOpenHashMap<>();
                    // CraftBukkit end
                })).put(resourceLocation, recipe);
                builder.put(resourceLocation, recipe.getType());
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                LOGGER.error("Parsing error loading recipe {}", resourceLocation, jsonparseexception);
            }
        }

        this.recipes = map.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (p_44033_) -> {
            return p_44033_.getValue().build();
        }));
        this.recipesCB = (Map) map1.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (entry1) -> {
            return (entry1.getValue()); // CraftBukkit
        }));
        this.byName = Maps.newHashMap(builder.build()); // CraftBukkit
        LOGGER.info(BannerMCStart.I18N.as("server.recipe.loaded"), (int)map.size());
    }

    /**
     * @author wdog5
     * @reason bukkit current recipe
     */
    /**
    @Overwrite
    public <C extends Container, T extends Recipe<C>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> recipeTypeIn, C inventoryIn, Level worldIn) {
        Optional<RecipeHolder<T>> optional = this.byType(recipeTypeIn).values().stream().filter((recipe) -> {
            return recipe.value().matches(inventoryIn, worldIn);
        }).findFirst();
        inventoryIn.setCurrentRecipe(optional.orElse(null)); // CraftBukkit - Clear recipe when no recipe is found
        return optional;
    }

    @Override
    public void addRecipe(RecipeHolder<?> irecipe) {
        if (this.recipes instanceof ImmutableMap) {
            this.recipes = new HashMap<>(recipes);
        }
        if (this.byName instanceof ImmutableMap) {
            this.byName = new HashMap<>(byName);
        }
        Map<ResourceLocation, RecipeHolder<?>> map = this.recipes.get(irecipe.value().getType());
        Object2ObjectLinkedOpenHashMap<ResourceLocation, RecipeHolder<?>> map0;
        if (!(map instanceof Object2ObjectLinkedOpenHashMap)) {
            Object2ObjectLinkedOpenHashMap<ResourceLocation, RecipeHolder<?>> hashMap = new Object2ObjectLinkedOpenHashMap<>();
            hashMap.putAll(map);
            this.recipes.put(irecipe.value().getType(), hashMap);
            map0 = hashMap;
        } else {
            map0 = ((Object2ObjectLinkedOpenHashMap<ResourceLocation, RecipeHolder<?>>) map);
        }

        Object2ObjectLinkedOpenHashMap<ResourceLocation, RecipeHolder<?>> craftbukkit = this.recipesCB.get(irecipe.value().getType()); // CraftBukkit

        if (this.byName.containsKey(irecipe.id()) || map0.containsKey(irecipe.id()) || craftbukkit.containsKey(irecipe.id())) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + irecipe.id());
        } else {
            map0.putAndMoveToFirst(irecipe.id(), irecipe);
            craftbukkit.putAndMoveToFirst(irecipe.id(), irecipe); // CraftBukkit - SPIGOT-4638: last recipe gets priority
            this.byName.put(irecipe.id(), irecipe);
        }
    }

    @Override
    public boolean removeRecipe(ResourceLocation mcKey) {
        if (this.recipes instanceof ImmutableMap) {
            this.recipes = new HashMap<>(recipes);
        }
        if (this.byName instanceof ImmutableMap) {
            this.byName = new HashMap<>(byName);
        }
        for (var recipes : recipes.values()) {
            if (!(recipes instanceof ImmutableMap)) {
                recipes.remove(mcKey);
            }
        }
        for (Object2ObjectLinkedOpenHashMap<ResourceLocation, RecipeHolder<?>> recipes0 : recipesCB.values()) {
            recipes0.remove(mcKey);
        }
        return byName.remove(mcKey) != null;
    }

    @Override
    public void clearRecipes() {
        this.recipesCB = Maps.newHashMap();
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            this.recipes.put(recipeType, ImmutableMap.of());
        }

        this.recipesCB = Maps.newHashMap();
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            this.recipesCB.put(recipeType, new Object2ObjectLinkedOpenHashMap<>());
        }
        this.byName = new HashMap<>();
    }

    @Override
    public Map<RecipeType<?>, Object2ObjectLinkedOpenHashMap<ResourceLocation, RecipeHolder<?>>> bridge$recipesCB() {
        return recipesCB;
    }*/
}
