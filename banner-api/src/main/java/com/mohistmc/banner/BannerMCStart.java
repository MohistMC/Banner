package com.mohistmc.banner;

import com.mohistmc.banner.config.BannerConfigUtil;
import com.mohistmc.banner.util.EulaUtil;
import com.mohistmc.banner.util.I18n;
import com.mohistmc.i18n.i18n;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;

public class BannerMCStart {

    public static i18n I18N;
    public static final Logger LOGGER = LogManager.getLogger("BannerMC");
    public static final float javaVersion = Float.parseFloat(System.getProperty("java.class.version"));

    public static void run() throws Exception {
        BannerConfigUtil.copyBannerConfig();
        BannerConfigUtil.lang();
        BannerConfigUtil.i18n();
        BannerConfigUtil.initAllNeededConfig();
        if (BannerConfigUtil.showLogo()) {
            LOGGER.info(" _____       ___   __   _   __   _   _____   _____   ");
            LOGGER.info("|  _  \\     /   | |  \\ | | |  \\ | | | ____| |  _  \\  ");
            LOGGER.info("| |_| |    / /| | |   \\| | |   \\| | | |__   | |_| |  ");
            LOGGER.info("|  _  {   / / | | | |\\   | | |\\   | |  __|  |  _  /  ");
            LOGGER.info("| |_| |  / /  | | | | \\  | | | \\  | | |___  | | \\ \\  ");
            LOGGER.info("|_____/ /_/   |_| |_|  \\_| |_|  \\_| |_____| |_|  \\_\\ ");
            LOGGER.info(I18n.as("banner.launch.welcomemessage") + " - " + getVersion() + ", Java " + javaVersion);
        }
        if (!EulaUtil.hasAcceptedEULA()) {
            System.out.println(I18n.as("eula"));
            while (!"true".equals(new Scanner(System.in).next()));
            EulaUtil.writeInfos();
        }
    }

    public static String getVersion() {
      return FabricLoader.getInstance().getModContainer("banner").get().getMetadata().getVersion().getFriendlyString();
    }
}
