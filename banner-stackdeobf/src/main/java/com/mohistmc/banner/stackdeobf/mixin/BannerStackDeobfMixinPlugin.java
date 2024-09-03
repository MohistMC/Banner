package com.mohistmc.banner.stackdeobf.mixin;

import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.config.BannerConfigUtil;
import com.mohistmc.banner.stackdeobf.mappings.CachedMappings;
import com.mohistmc.banner.stackdeobf.mappings.providers.MojangMappingProvider;
import com.mohistmc.banner.stackdeobf.util.RemappingRewritePolicy;
import com.mohistmc.banner.util.I18n;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class BannerStackDeobfMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        if (BannerConfigUtil.stackdeobf()) {
            BannerMCStart.LOGGER.info(I18n.as("stackdeobf.inject.logger"));
            RemappingRewritePolicy policy = new RemappingRewritePolicy();
            policy.inject((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger());
            CachedMappings.init(new MojangMappingProvider());
        }
    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
