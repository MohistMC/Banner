package com.mohistmc.banner.translate.mixin;

import com.mohistmc.banner.BannerMCStart;
import net.minecraft.advancements.AdvancementTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AdvancementTree.class)
public class MixinAdvancementTree {

    @ModifyConstant(method = "remove(Lnet/minecraft/advancements/AdvancementNode;)V",
            constant = @Constant(stringValue = "Forgot about advancement {}"))
    private String bosom$localForgot(String constant) {
        return BannerMCStart.I18N.as("advancement.forgot");
    }

    @ModifyConstant(method = "remove(Ljava/util/Set;)V",
            constant = @Constant(stringValue = "Told to remove advancement {} but I don't know what that is"))
    private String bosom$localRemove(String constant) {
        return BannerMCStart.I18N.as("advancement.remove.told");
    }

    @ModifyConstant(method = "addAll",
            constant = @Constant(stringValue = "Loaded {} advancements"))
    private String bosom$localAdvanceList(String constant) {
        return BannerMCStart.I18N.as("advancement.list");
    }
}
