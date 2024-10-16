package com.mohistmc.banner.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EulaUtil {
    private static final File eula = new File("eula.txt");
    private static final File globalEula = new File(System.getProperty("user.home"), "eula.txt");

    public static void writeInfos() throws IOException {
        eula.createNewFile();
        BufferedWriter b = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("eula.txt"), StandardCharsets.UTF_8));
        b.write(I18n.as("eula.text", "https://account.mojang.com/documents/minecraft_eula") + "\n" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "\neula=true");
        b.close();
    }

    public static boolean hasAcceptedEULA() throws IOException {
        return (globalEula.exists() && Files.readAllLines(globalEula.toPath()).contains("eula=true")) || (eula.exists() && Files.readAllLines(eula.toPath()).contains("eula=true"));
    }
}
