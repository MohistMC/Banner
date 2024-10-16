package com.mohistmc.banner.util;

import com.mohistmc.banner.BannerMCStart;

/**
 * @author Mgazul by MohistMC
 * @date 2023/9/23 6:15:26
 */
public class I18n {

    public static String as(String key) {
        return BannerMCStart.I18N.as(key);
    }

    public static String as(String key, Object... objects) {
        return BannerMCStart.I18N.as(key, objects);
    }
}
