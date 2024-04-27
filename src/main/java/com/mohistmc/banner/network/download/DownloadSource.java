package com.mohistmc.banner.network.download;

import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.config.BannerConfigUtil;
import com.mohistmc.tools.ConnectionUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum DownloadSource {

    MOHIST("https://maven.mohistmc.com/"),
    CHINA("https://libraries.mohistmc.cn:25119/releases/"),
    GITHUB("https://mohistmc.github.io/maven/");

    public static final DownloadSource defaultSource = isCN() ? CHINA : MOHIST;
    public final String url;

    public static DownloadSource get() {
        String ds = BannerConfigUtil.defaultSource();
        DownloadSource urL;
        for (DownloadSource me : DownloadSource.values()) {
            if (me.name().equalsIgnoreCase(ds)) {
                if (!ConnectionUtil.canAccess(me.url.replace("releases/", ""))) {
                    if (ds.equals("CHINA")) {
                        urL = MOHIST;
                        if (!ConnectionUtil.canAccess(urL.url)) {
                            return GITHUB;
                        }
                    }
                    return GITHUB;
                }
                return me;
            }
        }
        return defaultSource;
    }

    public static boolean isCN() {
        return BannerMCStart.I18N.isCN() && ConnectionUtil.measureLatency(CHINA.url.replace("releases/", "")) < ConnectionUtil.measureLatency(MOHIST.url);
    }
}
