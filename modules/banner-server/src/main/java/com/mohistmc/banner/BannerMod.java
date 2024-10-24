package com.mohistmc.banner;

import com.mohistmc.banner.eventhandler.BannerEventDispatcherRegistry;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BannerMod implements DedicatedServerModInitializer {

    public static final String MOD_ID = "banner";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeServer() {
        try {
            BannerMCStart.run();
        } catch (Exception ex) {
            BannerMCStart.LOGGER.error("Failed to load BannerServer..., caused by " + ex.getCause());
            throw new RuntimeException(ex);
        }
        BannerEventDispatcherRegistry.registerEventDispatchers();
    }

    public static String getVersion() {
        return FabricLoader.getInstance().getModContainer("banner").get().getMetadata().getVersion().getFriendlyString();
    }
}