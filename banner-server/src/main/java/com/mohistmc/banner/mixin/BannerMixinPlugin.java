package com.mohistmc.banner.mixin;

import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.asm.CreateConstructorProcessor;
import com.mohistmc.banner.asm.InlineFieldProcessor;
import com.mohistmc.banner.asm.InlineMethodProcessor;
import com.mohistmc.banner.asm.MixinProcessor;
import com.mohistmc.banner.asm.RenameIntoProcessor;
import com.mohistmc.banner.asm.TransformAccessProcessor;
import io.izzel.arclight.mixin.MixinTools;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IEnvironmentTokenProvider;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class BannerMixinPlugin implements IMixinConfigPlugin, IEnvironmentTokenProvider {

    private final List<MixinProcessor> preProcessors = List.of(
    );

    private final List<MixinProcessor> postProcessors = List.of(
            new RenameIntoProcessor(),
            new TransformAccessProcessor(),
            new CreateConstructorProcessor(),
            new InlineMethodProcessor(),
            new InlineFieldProcessor()
    );

    @Override
    public void onLoad(String mixinPackage) {
        MixinTools.setup();
        MixinEnvironment.getCurrentEnvironment().registerTokenProvider(this);
        try {
            BannerMCStart.run();
        } catch (Exception ex) {
            BannerMCStart.LOGGER.error("Failed to load BannerServer..., caused by " + ex.getCause());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Banner start - compat for arclight
         if (FabricLoader.getInstance().isModLoaded("arclight")) {
            return false;
        }
        // Banner end
        if (mixinClassName.equals("com.mohistmc.banner.mixin.core.world.entity.MixinMob$PaperSpawnAffect")) {
            return !FabricLoader.getInstance().isModLoaded("vmp");
        }
        if (mixinClassName.equals("com.mohistmc.banner.mixin.core.world.level.spawner.MixinNaturalSpawner")) {
            return !FabricLoader.getInstance().isModLoaded("carpet-tis-addition")
                    && !FabricLoader.getInstance().isModLoaded("carpet");
        }
        if (mixinClassName.equals("com.mohistmc.banner.mixin.core.network.protocol.MixinPacketUtils")) {
            return !FabricLoader.getInstance().isModLoaded("cobblemon");
        }
        if (mixinClassName.equals("com.mohistmc.banner.mixin.core.world.item.MixinChorusFruitItem")) {
            return !FabricLoader.getInstance().isModLoaded("openpartiesandclaims");
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        for (var processor : this.preProcessors) {
            processor.accept(targetClassName, targetClass, mixinInfo);
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        for (var processor : this.postProcessors) {
            processor.accept(targetClassName, targetClass, mixinInfo);
        }
        MixinTools.onPostMixin(targetClass);
    }

    @Override
    public int getPriority() {
        return 500;
    }

    @Override
    public Integer getToken(String token, MixinEnvironment env) {
        return null;
    }
}
