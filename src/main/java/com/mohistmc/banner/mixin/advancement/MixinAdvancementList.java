package com.mohistmc.banner.mixin.advancement;

import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementList.class)
public class MixinAdvancementList {

    @Inject(method = "add", at = @At(value = "INVOKE",
            target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
    private void banner$moveLogger(Map<ResourceLocation, Advancement.Builder> advancements, CallbackInfo ci) {}
    // CraftBukkit - moved to AdvancementDataWorld#reload

}
