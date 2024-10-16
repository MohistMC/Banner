package com.mohistmc.banner.config;

import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.i18n.i18n;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class BannerConfigUtil {

    public static final File bannerYml = new File("banner-config", "banner.yml");
    public static final YamlConfiguration yml = YamlConfiguration.loadConfiguration(bannerYml);

    public static void copyBannerConfig() {
        try {
            if (!bannerYml.exists()) {
                bannerYml.createNewFile();
            }
        } catch (Exception e) {
            System.out.println("File init exception!");
        }
    }

    public static boolean CHECK_LIBRARIES() {
        String key = "banner.check_libraries";
        if (yml.get(key) == null) {
            yml.set(key, true);
            save();
        }
        return yml.getBoolean(key, true);
    }

    public static boolean CHECK_UPDATE() {
        String key = "banner.check_update";
        if (yml.get(key) == null) {
            yml.set(key, true);
            save();
        }
        return yml.getBoolean(key, true);
    }

    public static boolean aBoolean(String key, boolean defaultReturn) {
        return yml.getBoolean(key, defaultReturn);
    }

    public static void save() {
        try {
            yml.save(bannerYml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void i18n() {
        String mohist_lang = yml.getString("banner.lang", Locale.getDefault().toString());
        BannerMCStart.I18N = new i18n(BannerMCStart.class.getClassLoader(), mohist_lang);
    }

    public static String lang() {
        String lang = "banner.lang";
        if (yml.get(lang) == null) {
            yml.set(lang, Locale.getDefault().toString());
            save();
        }
        return yml.getString(lang, Locale.getDefault().toString());
    }

    public static boolean showLogo() {
        String key = "banner.show_logo";
        if (yml.get(key) == null) {
            yml.set(key, true);
            save();
        }
        return yml.getBoolean(key, true);
    }

    public static boolean stackdeobf() {
        String key = "banner.stackdeobf";
        if (yml.get(key) == null) {
            yml.set(key, true);
            save();
        }
        return yml.getBoolean(key, true);
    }

    public static boolean isCN() {
        return BannerMCStart.I18N.isCN();
    }

    public static boolean skipOtherWorldPreparing() {
        String key = "compat.skipOtherWorldPreparing";
        if (yml.get(key) == null) {
            yml.set(key, true);
            save();
        }
        return yml.getBoolean(key, true);
    }

    public static int serverThread() {
        String key = "threadpriority.server_thread";
        if (yml.get(key) == null) {
            yml.set(key, 8);
            save();
        }
        return yml.getInt(key, 8);
    }

    public static String motdFirstLine() {
        String key = "motd.firstline";
        if (yml.get(key) == null) {
            yml.set(key, "<RAINBOW1>A Minecraft Server</RAINBOW>");
            save();
        }
        return yml.getString(key);
    }

    public static String motdSecondLine() {
        String key = "motd.secondline";
        if (yml.get(key) == null) {
            yml.set(key, "");
            save();
        }
        return yml.getString(key);
    }

    public static void initAllNeededConfig() {
        skipOtherWorldPreparing();
        serverThread();
        motdFirstLine();
        motdSecondLine();
        stackdeobf();
    }

}
