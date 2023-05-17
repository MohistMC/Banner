package com.mohistmc.banner.mixin.server;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.PredicateManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Map;

@Mixin(ServerAdvancementManager.class)
public class MixinServerAdvancementManager {

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final private PredicateManager predicateManager;


    @Shadow public AdvancementList advancements;

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
                Advancement.Builder builder = Advancement.Builder.fromJson(jsonObject, new DeserializationContext(resourceLocation, this.predicateManager));
                map.put(resourceLocation, builder);
            } catch (Exception var6) {
                LOGGER.error("Parsing error loading custom advancement {}: {}", resourceLocation, var6.getMessage());
            }

        });
        AdvancementList advancementList = new AdvancementList();
        advancementList.add(map);
        Iterator var6 = advancementList.getRoots().iterator();

        while(var6.hasNext()) {
            Advancement advancement = (Advancement)var6.next();
            if (advancement.getDisplay() != null) {
                TreeNodePosition.run(advancement);
            }
        }

        this.advancements = advancementList;
    }

}
