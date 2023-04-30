package com.mohistmc.banner.config;

public class BannerConfig {

    @Config(category = "banner", key = "lang", comment = "Decide your banner language.")
    public static String banner_lang = "xx_XX";

    @Config(category = "banner", key = "showLogo", comment = "Decide to show Banner Logo")
    public static boolean showLogo = true;

    public static void setup() {
        new Configuration(BannerConfig.class);
    }

}
