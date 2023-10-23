package com.mohistmc.banner.libraries;

import com.mohistmc.banner.network.download.DownloadSource;
import com.mohistmc.banner.network.download.UpdateUtils;
import com.mohistmc.banner.util.I18n;
import com.mohistmc.tools.MD5Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import net.fabricmc.loader.api.FabricLoader;

public class DefaultLibraries {

    public static final HashMap<String, String> fail = new HashMap<>();
    public static final AtomicLong allSize = new AtomicLong(); // global
    public static final String MAVENURL = DownloadSource.get().getUrl();


    public static String libUrl(File lib) {
        return MAVENURL + "libraries/" + lib.getAbsolutePath().replaceAll("\\\\", "/").split("/libraries/")[1];
    }

    public static void run() throws Exception {
        System.out.println(I18n.as("libraries.checking.start"));
        System.out.println(I18n.as("libraries.downloadsource", DownloadSource.get().name()));
        LinkedHashMap<File, String> libs = getDefaultLibs();
        AtomicLong currentSize = new AtomicLong();
        Set<File> defaultLibs = new LinkedHashSet<>();
        for (File lib : getDefaultLibs().keySet()) {
            if (lib.exists() && MD5Util.get(lib).equals(libs.get(lib))) {
                currentSize.addAndGet(lib.length());
                continue;
            }
            defaultLibs.add(lib);
        }
        for (File lib : defaultLibs) {
            lib.getParentFile().mkdirs();

            String u = libUrl(lib);
            System.out.println(I18n.as("libraries.global.percentage", Math.round((float) (currentSize.get() * 100) / allSize.get()) + "%")); //Global percentage
            try {
                UpdateUtils.downloadFile(u, lib, libs.get(lib), true);
                currentSize.addAndGet(lib.length());
                fail.remove(u.replace(MAVENURL, ""));
            } catch (Exception e) {
                if (e.getMessage() != null && !"md5".equals(e.getMessage())) {
                    System.out.println(I18n.as("file.download.nook", u));
                    lib.delete();
                }
                fail.put(u.replace(MAVENURL, ""), lib.getAbsolutePath());
            }
        }
        /*FINISHED | RECHECK IF A FILE FAILED*/
        if (!fail.isEmpty()) {
            run();
        } else {
            System.out.println(I18n.as("libraries.check.end"));
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
            allSize.addAndGet(Long.parseLong(s[2]));
        }
        b.close();
        return temp;
    }

}