package com.mohistmc.banner.mixin.world.level.block.entity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BannerBlockEntity.class)
public class MixinBannerBlockEntity {

    @Shadow private BannerPatternLayers patterns;

    @Inject(method = "applyImplicitComponents", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/level/block/entity/BannerBlockEntity;name:Lnet/minecraft/network/chat/Component;"))
    private void banner$checkPattern(BlockEntity.DataComponentInput dataComponentInput, CallbackInfo ci) {
        this.setPatterns((BannerPatternLayers) dataComponentInput.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)); // CraftBukkit - apply limits
    }

    public void setPatterns(BannerPatternLayers bannerpatternlayers) {
        if (bannerpatternlayers.layers().size() > 20) {
            bannerpatternlayers = new BannerPatternLayers(List.copyOf(bannerpatternlayers.layers().subList(0, 20)));
        }
        this.patterns = bannerpatternlayers;
    }
}
