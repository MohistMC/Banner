package com.mohistmc.banner.libraries;

import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.network.download.DownloadSource;
import com.mohistmc.banner.network.download.UpdateUtils;
import com.mohistmc.banner.util.MD5Util;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultLibraries {

    public static final HashMap<String, String> fail = new HashMap<>();
    public static final String MAVENURL;

    static {
        try {
            MAVENURL = DownloadSource.get().getUrl();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String libUrl(File lib) {
        return MAVENURL + "libraries/" + lib.getAbsolutePath().replaceAll("\\\\", "/").split("/libraries/")[1];
    }

    public static void run() throws Exception {
        System.out.println(BannerMCStart.I18N.as("libraries.checking.start"));
        LinkedHashMap<File, String> libs = getDefaultLibs();
        AtomicLong currentSize = new AtomicLong();
        Set<File> defaultLibs = new LinkedHashSet<>();
        AtomicLong allSize = new AtomicLong(); // global
        for (File lib : getDefaultLibs().keySet()) {
            if (lib.exists() && MD5Util.getMd5(lib).equals(libs.get(lib))) {
                currentSize.addAndGet(lib.length());
                continue;
            }
            allSize.addAndGet(UpdateUtils.getAllSizeOfUrl(libUrl(lib)));
            defaultLibs.add(lib);
        }
        for (File lib : defaultLibs) {
            lib.getParentFile().mkdirs();

            String u = libUrl(lib);
            System.out.println(BannerMCStart.I18N.as("libraries.global.percentage") + Math.round((float) (currentSize.get() * 100) / allSize.get()) + "%"); //Global percentage
            try {
                UpdateUtils.downloadFile(u, lib, libs.get(lib));
                currentSize.addAndGet(lib.length());
                fail.remove(u.replace(MAVENURL, ""));
            } catch (Exception e) {
                if (e.getMessage() != null && !"md5".equals(e.getMessage())) {
                    System.out.println(BannerMCStart.I18N.as("file.download.nook", u));
                    lib.delete();
                }
                fail.put(u.replace(MAVENURL, ""), lib.getAbsolutePath());
            }
        }
        /*FINISHED | RECHECK IF A FILE FAILED*/
        if (!fail.isEmpty()) {
            run();
        } else {
            System.out.println(BannerMCStart.I18N.as("libraries.check.end"));
        }
    }

    public static void proposeFabricLibs() throws Exception {
        Set<File> defaultLibs = new LinkedHashSet<>(getDefaultLibs().keySet());
        for (File lib : defaultLibs) {
            if (lib.exists()) {
                KnotLibraryHelper.propose(lib);
            }
        }
    }

    public static LinkedHashMap<File, String> getDefaultLibs() throws Exception {
        LinkedHashMap<File, String> temp = new LinkedHashMap<>();
        BufferedReader b = new BufferedReader(new InputStreamReader(Objects.requireNonNull(DefaultLibraries.class.getClassLoader().getResourceAsStream("libraries.txt"))));
        String str;
        while ((str = b.readLine()) != null) {
            String[] s = str.split("\\|");
            temp.put(new File(FabricLoader.getInstance().getGameDir() + "/" + s[0]), s[1]);
        }
        b.close();
        return temp;
    }

}