package com.mohistmc.banner.mixin.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(ServerAdvancementManager.class)
public class MixinServerAdvancementManager {

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final private PredicateManager predicateManager;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V", shift = At.Shift.BY), cancellable = true)
    private void banner$cancelForEach(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "NEW", target = "Lnet/minecraft/advancements/AdvancementList;<init>()V", shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD,
            remap = false,
            cancellable = true)
    private void banner$forEachNew(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci, Map map) {
        object.forEach((resourceLocation, jsonElement) -> {
            // Spigot start
            if (org.spigotmc.SpigotConfig.disabledAdvancements != null && (org.spigotmc.SpigotConfig.disabledAdvancements.contains("*") || org.spigotmc.SpigotConfig.disabledAdvancements.contains(resourceLocation.toString()) || org.spigotmc.SpigotConfig.disabledAdvancements.contains(resourceLocation.getNamespace()))) {
                ci.cancel();
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
    }
}
