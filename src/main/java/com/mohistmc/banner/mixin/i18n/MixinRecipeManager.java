package com.mohistmc.banner.mixin.i18n;

import com.mohistmc.banner.BannerMCStart;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(RecipeManager.class)
public class MixinRecipeManager {

    @ModifyConstant(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            constant = @Constant(stringValue = "Loaded {} recipes"))
    private String banner$i18nRecipe(String constant) {
        return BannerMCStart.I18N.get("server.recipe.loaded");
    }
}
