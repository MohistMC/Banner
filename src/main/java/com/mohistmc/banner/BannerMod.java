package com.mohistmc.banner;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BannerMod implements ModInitializer {

    public static final String MOD_ID = "banner";
    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        MixinExtrasBootstrap.init();
        LOGGER.info("Hello Fabric world!");
    }
}