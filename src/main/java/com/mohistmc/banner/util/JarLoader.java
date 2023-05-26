package com.mohistmc.banner.util;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.jar.JarFile;

public class JarLoader {

    private static Instrumentation inst = null;

    public JarLoader() {
    }

    // The JRE will call method before launching your main()
    public static void agentmain(final String a, final Instrumentation inst) {
        JarLoader.inst = inst;
    }

    // Don't forget to specify -javaagent:<banner jar> on Java 9+,
    // if you load the main Banner jar from -cp rather than direct-jar
    public static void premain(String agentArgs, Instrumentation inst) {
        JarLoader.inst = inst;
    }

    public static void loadJar(Path path) {
        if (!path.toFile().getName().endsWith(".jar")) {
            return;
        }
        try {
            inst.appendToSystemClassLoaderSearch(new JarFile(path.toFile()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}