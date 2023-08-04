package com.mohistmc.banner.config;

import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.network.download.DownloadSource;
import com.mohistmc.i18n.i18n;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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

    public static String defaultSource() {
        String key = "banner.libraries_downloadsource";
        if (yml.get(key) == null) {
            yml.set(key, DownloadSource.defaultSource.name());
            save();
        }
        return yml.getString(key, DownloadSource.defaultSource.name());
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
        String banner_lang = yml.getString("banner.lang", "xx_XX");
        String l = banner_lang.split("_")[0];
        String c = banner_lang.split("_")[1];
        BannerMCStart.I18N = new i18n(BannerMCStart.class.getClassLoader(), new Locale(l, c));
    }

    public static String lang() {
        String lang = "banner.lang";
        if (yml.get(lang) == null) {
            yml.set(lang, "xx_XX");
            save();
        }
        return yml.getString(lang, "xx_XX");
    }

    public static boolean showLogo() {
        String key = "banner.show_logo";
        if (yml.get(key) == null) {
            yml.set(key, true);
            save();
        }
        return yml.getBoolean(key, true);
    }

    public static List<String> BLACKLIST_LIB() {
        return yml.getStringList("libraries_black_list");
    }

    public static boolean isCN() {
        TimeZone timeZone = TimeZone.getDefault();
        return "Asia/Shanghai".equals(timeZone.getID());
    }

    public static boolean skipOtherWorldPreparing() {
        String key = "compat.skipOtherWorldPreparing";
        if (yml.get(key) == null) {
            yml.set(key, false);
            save();
        }
        return yml.getBoolean(key, false);
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
    }

}
