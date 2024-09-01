package com.mohistmc.banner.translate.mixin;

import com.mohistmc.banner.BannerMCStart;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.util.Mth;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LoggerChunkProgressListener.class)
public abstract class MixinLoggerChunkProgressListener {

    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract int getProgress();

    @Redirect(method = "onStatusChange", at = @At(value = "INVOKE",
            target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V",
            remap = false))
    private void bosom$i18nPrepArea(Logger instance, String s) {
        LOGGER.info(BannerMCStart.I18N.as("world.preparingSpawn"), Mth.clamp(this.getProgress(), 0, 100));
    }

    @ModifyConstant(method = "stop", constant = @Constant(stringValue = "Time elapsed: {} ms"))
    private String bosom$i18nTime(String constant) {
        return BannerMCStart.I18N.as("world.time.elapsed");
    }
}
