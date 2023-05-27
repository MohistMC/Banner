package com.mohistmc.banner.mixin.i18n;

import com.mohistmc.banner.BannerMCStart;
import net.minecraft.advancements.AdvancementList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AdvancementList.class)
public class MixinAdvancementList {

    @ModifyConstant(method = "remove(Lnet/minecraft/advancements/Advancement;)V",
            constant = @Constant(stringValue = "Forgot about advancement {}"))
    private String banner$localForgot(String constant) {
        return BannerMCStart.I18N.get("advancement.forgot");
    }

    @ModifyConstant(method = "remove(Ljava/util/Set;)V",
            constant = @Constant(stringValue = "Told to remove advancement {} but I don't know what that is"))
    private String banner$localRemove(String constant) {
        return BannerMCStart.I18N.get("advancement.remove.told");
    }

    @ModifyConstant(method = "add",
            constant = @Constant(stringValue = "Loaded {} advancements"))
    private String banner$localAdvanList(String constant) {
        return BannerMCStart.I18N.get("advancement.list");
    }
}
