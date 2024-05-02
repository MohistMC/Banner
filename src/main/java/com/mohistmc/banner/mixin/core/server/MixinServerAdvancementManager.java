package com.mohistmc.banner.mixin.core.server;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;

import java.util.Iterator;
import java.util.Map;

import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerAdvancementManager.class)
public abstract class MixinServerAdvancementManager {

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final private LootDataManager lootData;

    @Shadow private Map<ResourceLocation, AdvancementHolder> advancements;

    @Shadow private AdvancementTree tree;

    @Shadow protected abstract void validate(ResourceLocation resourceLocation, Advancement advancement);

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, AdvancementHolder> builder = ImmutableMap.builder();
        map.forEach((resourceLocation, jsonElement) -> {
            // Spigot start
            if (org.spigotmc.SpigotConfig.disabledAdvancements != null
                    && (org.spigotmc.SpigotConfig.disabledAdvancements.contains("*")
                    || org.spigotmc.SpigotConfig.disabledAdvancements.contains(resourceLocation.toString())
                    || org.spigotmc.SpigotConfig.disabledAdvancements.contains(resourceLocation.getNamespace()))) {
                return;
            }
            // Spigot end
            try {
                Advancement advancement = (Advancement) Util.getOrThrow(Advancement.CODEC.parse(JsonOps.INSTANCE, jsonElement), JsonParseException::new);
                this.validate(resourceLocation, advancement);
                builder.put(resourceLocation, new AdvancementHolder(resourceLocation, advancement));
            } catch (Exception var5) {
                LOGGER.error("Parsing error loading custom advancement {}: {}", resourceLocation, var5.getMessage());
            }
        });
        this.advancements = builder.buildOrThrow();
        AdvancementTree advancementTree = new AdvancementTree();
        advancementTree.addAll(this.advancements.values());
        Iterator var6 = advancementTree.roots().iterator();

        while(var6.hasNext()) {
            AdvancementNode advancementNode = (AdvancementNode)var6.next();
            if (advancementNode.holder().value().display().isPresent()) {
                TreeNodePosition.run(advancementNode);
            }
        }

        this.tree = advancementTree;
    }

}
