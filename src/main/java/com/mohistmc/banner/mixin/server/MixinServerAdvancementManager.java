package com.mohistmc.banner.mixin.server;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerAdvancementManager.class)
public class MixinServerAdvancementManager {

    @Shadow @Final private static Logger LOGGER;

    @Shadow public AdvancementList advancements;

    @Shadow @Final private LootDataManager lootData;

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, Advancement.Builder> map = Maps.newHashMap();
        object.forEach((resourceLocation, jsonElement) -> {
            // Spigot start
            if (org.spigotmc.SpigotConfig.disabledAdvancements != null
                    && (org.spigotmc.SpigotConfig.disabledAdvancements.contains("*")
                    || org.spigotmc.SpigotConfig.disabledAdvancements.contains(resourceLocation.toString())
                    || org.spigotmc.SpigotConfig.disabledAdvancements.contains(resourceLocation.getNamespace()))) {
                return;
            }
            // Spigot end
            try {
                JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "advancement");
                Advancement.Builder builder = Advancement.Builder.fromJson(jsonObject, new DeserializationContext(resourceLocation, this.lootData));
                map.put(resourceLocation, builder);
            } catch (Exception var6) {
                LOGGER.error("Parsing error loading custom advancement {}: {}", resourceLocation, var6.getMessage());
            }

        });
        AdvancementList advancementList = new AdvancementList();
        advancementList.add(map);

        for (Advancement advancement : advancementList.getRoots()) {
            if (advancement.getDisplay() != null) {
                TreeNodePosition.run(advancement);
            }
        }

        this.advancements = advancementList;
    }

}
