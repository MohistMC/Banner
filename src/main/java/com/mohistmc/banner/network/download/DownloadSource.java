package com.mohistmc.banner.network.download;

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
    GITHUB("https://mohistmc.github.io/maven/");

    public final String url;

    public static DownloadSource get() {
        String ds = BannerConfigUtil.defaultSource();
        for (DownloadSource me : DownloadSource.values()) {
            if (me.name().equalsIgnoreCase(ds)) {
                if (!ConnectionUtil.canAccess(me.url)) {
                    return GITHUB;
                }
                return me;
            }
        }
        return MOHIST;
    }
}
