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
        String showLogo = """
                 _____       ___   __   _   __   _   _____   _____  \s
                |  _  \\     /   | |  \\ | | |  \\ | | | ____| |  _  \\ \s
                | |_| |    / /| | |   \\| | |   \\| | | |__   | |_| | \s
                |  _  {   / / | | | |\\   | | |\\   | |  __|  |  _  / \s
                | |_| |  / /  | | | | \\  | | | \\  | | |___  | | \\ \\ \s
                |_____/ /_/   |_| |_|  \\_| |_|  \\_| |_____| |_|  \\_\\\s
                """;
        LOGGER.info(showLogo);
        LOGGER.info("Welcome to use Mohist Banner!");
    }
}