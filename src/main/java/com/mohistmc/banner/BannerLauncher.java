package com.mohistmc.banner;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Properties;

public class BannerLauncher {

    private static final int MIN_CLASS_VERSION = 61;
    private static final int MIN_JAVA_VERSION = 17;

    private static final int MAX_CLASS_VERSION = 67;
    private static final int MAX_JAVA_VERSION = 23;

    public static void main(String[] args) throws Throwable {
        int javaVersion = (int) Float.parseFloat(System.getProperty("java.class.version"));
        if (javaVersion < MIN_CLASS_VERSION) {
            System.err.println("Banner requires Java " + MIN_JAVA_VERSION);
            System.err.println("Current: " + System.getProperty("java.version"));
            System.exit(-1);
            return;
        }

        if (javaVersion > MAX_CLASS_VERSION) {
            System.err.println("Warning: Banner is known to be compatible with up to Java " + MAX_JAVA_VERSION + " and may not run on later versions");
            System.err.println("Current: " + System.getProperty("java.version"));
            System.err.flush();
            Thread.sleep(3000);
        }

        try (InputStream input = BannerLauncher.class.getResourceAsStream("/banner-server-launch.properties")) {
            Properties properties = new Properties();
            properties.load(input);

            String target = properties.getProperty("launch.mainClass");
            MethodHandle main = MethodHandles.lookup().findStatic(Class.forName(target), "main", MethodType.methodType(void.class, String[].class));
            main.invoke((Object) args);
        }
    }
}
