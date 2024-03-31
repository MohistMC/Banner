package com.mohistmc.banner.network.download;

import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.util.I18n;
import com.mohistmc.mjson.Json;
import com.mohistmc.tools.ConnectionUtil;
import com.mohistmc.tools.MD5Util;
import com.mohistmc.tools.NumberUtil;
import java.io.File;
import java.net.URI;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UpdateUtils {

    private static int percentage = 0;

    public static void versionCheck() {
        System.out.println(I18n.as("update.check"));
        System.out.println(I18n.as("update.stopcheck"));

        try {
            Json json = Json.read(URI.create("https://ci.codemc.io/job/MohistMC/job/Banner-1.20/lastSuccessfulBuild/api/json").toURL());

            String jar_sha = BannerMCStart.getVersion();
            String build_number = json.asString("number");
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(json.asLong("timestamp")));

            if (jar_sha.equals(build_number))
                System.out.println(I18n.as("update.latest", jar_sha, build_number));
            else {
                System.out.println(I18n.as("update.detect", build_number, jar_sha, time));
            }
        } catch (Throwable e) {
            System.out.println(I18n.as("check.update.noci"));
        }
    }

    public static void downloadFile(String URL, File f) throws Exception {
        downloadFile(URL, f, null, true);
    }

    public static void downloadFile(String URL, File f, String md5, boolean showlog) throws Exception {
        URLConnection conn = ConnectionUtil.getConn(URL);
        if (showlog) System.out.println(I18n.as("download.file", f.getName(), NumberUtil.getSize(conn.getContentLength())));
        ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
        FileChannel fc = FileChannel.open(f.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        int fS = conn.getContentLength();

        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2);
        scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                    if (rbc.isOpen()) {
                        if (percentage != Math.round((float) f.length() / fS * 100) && percentage < 100) {
                            System.out.println(I18n.as("file.download.percentage", f.getName(), percentage));
                        }
                        percentage = Math.round((float) f.length() / fS * 100);
                    }
                }, 3000, 1000, TimeUnit.SECONDS);

        fc.transferFrom(rbc, 0, Long.MAX_VALUE);
        fc.close();
        rbc.close();
        percentage = 0;
        String MD5 = MD5Util.get(f);
        if (f.getName().endsWith(".jar") && md5 != null && MD5 != null && !MD5.equals(md5.toLowerCase())) {
            f.delete();
            if (showlog) System.out.println(I18n.as("file.download.nook.md5", URL, MD5, md5.toLowerCase()));
            throw new Exception("md5");
        }
        if (showlog) System.out.println(I18n.as("download.file.ok", f.getName()));
    }
}
