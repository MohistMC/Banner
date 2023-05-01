package com.mohistmc.banner.bukkit.nms;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import net.md_5.specialsource.SpecialSource;

public class Remapper {

    public static List<Provider> providers = new ArrayList<>();
    public static void addProvider(Provider provider) {
        providers.add(provider);
    }

    public static int MAPPINGS_VERSION = 105;

    public static File libDir = new File(FabricLoader.getInstance().getGameDir().toFile(), ".fabric");
    public static File remappedDir = new File(libDir, "remapped-plugins");
    public static File backup = new File(remappedDir, "backup-plugins");
    public static File spigot2inter;
    public static File md5info = new File(remappedDir, "md5-hashes.dat");

    public static List<String> hashes = new ArrayList<>();

    /**
     * Remaps NMS used in plugins<br><br>
     *
     * 1. Maps Spigot-NMS to Fabric intermediary<br>
     * 2. Maps intermediary to obf.<br><br>
     *
     * These steps will hopefully allow plugins to use NMS during snapshots
     */
    public static void remap(File jarFile) {
        if (!libDir.exists()) {
            libDir.mkdir();
        }
        remappedDir.mkdirs();
        backup.mkdirs();

        for (Provider p : Remapper.providers) {
            boolean b = p.remap(jarFile);
            if (b) return;
        }

        try {
            md5info.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        spigot2inter = new File(remappedDir, "spigot2intermediary.csrg");
        exportResource("spigot2intermediary.csrg", remappedDir);
        String md5 = null;
        try (InputStream is = Files.newInputStream(jarFile.toPath())) {
            md5 = com.mohistmc.banner.bukkit.nms.DigestUtil.md5Hex(is);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        File toMap = jarFile;
        boolean usingBackup = false;
        if (hashes.size() <= 0 || !hashes.get(0).equals("mappings=" + MAPPINGS_VERSION)) {
            toMap = new File(backup, jarFile.getName());
            if (!toMap.exists()) {
                toMap = jarFile;
                usingBackup = false;
            } else usingBackup = true;
        }

        if (hashes.contains(md5) && hashes.get(0).equals("mappings=" + MAPPINGS_VERSION)) return;

        String jarName = jarFile.getName().substring(0, jarFile.getName().indexOf(".jar"));
        //BannerServer.LOGGER.info("Remapping \"" + jarFile + "\"...");

        // Spigot -> Intermediary
        File finalJar = new File(remappedDir, jarName + "-intermediary.jar");
        runSpecialSource(spigot2inter, toMap, finalJar);

        if (!usingBackup) {
            try {
                Files.copy(jarFile.toPath(), new File(backup, jarFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Files.copy(finalJar.toPath(), jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = Files.newInputStream(jarFile.toPath())) {
            md5 = com.mohistmc.banner.bukkit.nms.DigestUtil.md5Hex(is);
        } catch (IOException e1) {
            md5 = null;
            e1.printStackTrace();
        }
        if (null != md5) hashes.add(md5);
        finalJar.delete();
        saveHashes();
    }

    public static void saveHashes() {
        String out = "mappings=" + MAPPINGS_VERSION + "\n";
        for (String hash : hashes)
            out += hash + "\n";
        try {
            Files.write(md5info.toPath(), out.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runSpecialSource(File mappingsFile, File inJar, File outJar) {
        for (Provider p : Remapper.providers) {
            boolean b = p.runSpecialSource(mappingsFile, inJar, outJar);
            if (b) return;
        }

        String[] args = {"-q", "-i", inJar.getAbsolutePath(), "-o", outJar.getAbsolutePath(), "-m", mappingsFile.getAbsolutePath()};
        try {
            SpecialSource.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Path exportResource(String res, File folder) {
        try (InputStream stream = Remapper.class.getClassLoader().getResourceAsStream("mappings/" + res)) {
            if (stream == null) throw new IOException("Null " + res);

            File f = new File(folder, res);
            f.createNewFile();
            Files.copy(stream, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return f.toPath();
        } catch (IOException e) { e.printStackTrace(); return null;}
    }

}