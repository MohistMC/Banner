package com.mohistmc.banner.network.download;

import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.config.BannerConfigUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

@ToString
@AllArgsConstructor
public enum DownloadSource {

    MOHIST("https://maven.mohistmc.com/"),
    CHINA("http://s1.devicloud.cn:25119/"),
    GITHUB("https://mavenmirror.mohistmc.com/");

    public static final DownloadSource defaultSource = isCN() ? CHINA : MOHIST;
    @Getter
    final
    String url;

    public static DownloadSource get() throws IOException {
        String ds = BannerConfigUtil.defaultSource();
        for (DownloadSource me : DownloadSource.values()) {
            if (me.name().equalsIgnoreCase(ds)) {
                if (isDown(me.url) != 200) return GITHUB;
                return me;
            }
        }
        return defaultSource;
    }

    public static boolean isCN() {
        return BannerMCStart.I18N.isCN();
    }

    public static int isDown(String s) throws IOException {
        URL url = new URL(s);
        URLConnection rulConnection = url.openConnection();
        HttpURLConnection httpUrlConnection = (HttpURLConnection) rulConnection;
        httpUrlConnection.connect();
        return httpUrlConnection.getResponseCode();
    }
}
