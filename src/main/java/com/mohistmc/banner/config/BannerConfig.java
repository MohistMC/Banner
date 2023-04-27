package com.mohistmc.banner.config;

public class BannerConfig {

    @Config(category = "banner", key = "lang", comment = "Decide your banner language.")
    public static String banner_lang = "xx_XX";

    public static void setup() {
        new Configuration(BannerConfig.class);
    }

}
