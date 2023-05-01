package com.mohistmc.banner.bukkit.nms;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

import com.mohistmc.banner.BannerServer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;

/**
 * Very unsafe re-mapping of Reflection.
 */
public class ReflectionRemapper {

    public static Class<?> getClassForName(String className) throws ClassNotFoundException {
        return getClassFromJPL(className);
    }

    public static Field getFieldByName(Class<?> calling, String f) throws ClassNotFoundException {
        try {
            Field field = calling.getDeclaredField(MappingsReader.getIntermedField(calling.getName(), f));
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            try {
                Field a = calling.getDeclaredField(MappingsReader.getIntermedField(calling.getName(), f));
                a.setAccessible(true);
                return a;
            } catch (NoSuchFieldException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    Field a = whyIsAsmBroken.getDeclaredField(MappingsReader.getIntermedField(whyIsAsmBroken.getName(), f));
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchFieldException | SecurityException e2) {
                    if (f.contains("B_STATS_VERSION")) {
                        return getBstatsVersionField();
                    }
                    //System.out.println("DeBug:" + calling.getName() + " / " + getCallerClassName());
                    e2.printStackTrace();
                }
                return null;
            }
        }
    }

    private static int BV_CALLED = 0;
    public static Field getBstatsVersionField() {
        Field f = null;
        int i = 0;
        for (final Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
            if (i < BV_CALLED) {
                i++;
                continue;
            }
            try {
                f = service.getField("B_STATS_VERSION"); // Identifies bStats classes
                break;
            } catch (final NoSuchFieldException ignored) {
            }
        }
        BV_CALLED++;
        return f;
    }

    public static Field getDeclaredFieldByName(Class<?> calling, String f) throws ClassNotFoundException, NoSuchFieldException {
        try {
            return calling.getDeclaredField(MappingsReader.getIntermedField(calling.getName(), f));
        } catch (NoSuchFieldException | SecurityException e) {
            try {
                Field a = calling.getDeclaredField(MappingsReader.getIntermedField(calling.getName(), f));
                a.setAccessible(true);
                return a;
            } catch (NoSuchFieldException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    if (null == whyIsAsmBroken) {
                        System.out.println("CALLING: " + calling.getName() + ", F: " + f);
                        return null;
                    }
                    Field a = whyIsAsmBroken.getDeclaredField(MappingsReader.getIntermedField(whyIsAsmBroken.getName(), f));
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchFieldException | SecurityException e2) {
                    throw e2;
                    //e1.printStackTrace();
                }
                // return null;
            }
        }
    }

    public static Method getMethodByName(Class<?> calling, String f) throws ClassNotFoundException, NoSuchMethodException {
        Method m = getDeclaredMethodByName(calling, f);
        m.setAccessible(true);
        return m;
    }

    public static Method[] getMethods(Class<?> calling) {
        Method[] r = calling.getMethods();
        if (calling.getSimpleName().contains("MinecraftServer")) {
            Method[] nr = new Method[r.length+1];
            for (int i = 0; i < r.length; i++) {
                nr[i] = r[i];
            }
            return nr;
        }
        return r;
    }

    public static Method getDeclaredMethodByName(Class<?> calling, String f) throws ClassNotFoundException, NoSuchMethodException {
        try {
            return calling.getMethod(MappingsReader.getIntermedMethod(calling.getName(), f));
        } catch (NoSuchMethodException | SecurityException e) {
            try {
                Method a = calling.getDeclaredMethod(MappingsReader.getIntermedMethod(calling.getName(), f));
                a.setAccessible(true);
                return a;
            } catch (NoSuchMethodException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    Method a = whyIsAsmBroken.getDeclaredMethod(MappingsReader.getIntermedMethod(whyIsAsmBroken.getName(), f));
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchMethodException | SecurityException e2) {
                    throw e2;
                    //e1.printStackTrace();
                }
                //return null;
            }
        }
    }

    public static Method getDeclaredMethodByName(Class<?> calling, String f, Class<?>[] parms) throws ClassNotFoundException, NoSuchMethodException {
        try {
            return calling.getMethod(MappingsReader.getIntermedMethod(calling.getName(), f, parms), parms);
        } catch (NoSuchMethodException | SecurityException e) {
            try {
                Method a = calling.getDeclaredMethod(MappingsReader.getIntermedMethod(calling.getName(), f, parms), parms);
                a.setAccessible(true);
                return a;
            } catch (NoSuchMethodException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    Method a = whyIsAsmBroken.getDeclaredMethod(MappingsReader.getIntermedMethod(whyIsAsmBroken.getName(), f), parms);
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchMethodException | SecurityException e2) {
                    e1.printStackTrace();
                }
                return getDeclaredMethodByName(calling, f);
            }
        }
    }

    /**
     * Retrieve a class that is from a plugin
     *
     * @author Isaiah
     */
    @SuppressWarnings("unchecked")
    public static Class<?> getClassFromJPL(String name) {
        try {
            SimplePluginManager pm = (SimplePluginManager) Bukkit.getPluginManager();
            Field fa = SimplePluginManager.class.getDeclaredField("fileAssociations");
            fa.setAccessible(true);
            Map<Pattern, PluginLoader> pl = (Map<Pattern, PluginLoader>) fa.get(pm);
            JavaPluginLoader jpl = null;
            for (PluginLoader loader : pl.values()) {
                if (loader instanceof JavaPluginLoader) {
                    jpl = (JavaPluginLoader) loader;
                    break;
                }
            }

            Method fc = JavaPluginLoader.class.getDeclaredMethod("getClassByName", String.class);
            fc.setAccessible(true);
            return (Class<?>) fc.invoke(jpl, name);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            BannerServer.LOGGER.warn("SOMETHING EVERY WRONG! PLEASE REPORT THE EXCEPTION BELOW TO BUKKIT4FABRIC:");
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static JavaPluginLoader getFirstJPL() {
        try {
            SimplePluginManager pm = (SimplePluginManager) Bukkit.getPluginManager();
            if (null == pm) System.out.println(" NULL PM ");
            Field fa = SimplePluginManager.class.getDeclaredField("fileAssociations");
            fa.setAccessible(true);
            Map<Pattern, PluginLoader> pl = (Map<Pattern, PluginLoader>) fa.get(pm);
            JavaPluginLoader jpl = null;
            for (PluginLoader loader : pl.values()) {
                if (loader instanceof JavaPluginLoader) {
                    jpl = (JavaPluginLoader) loader;
                    break;
                }
            }
            return jpl;
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            BannerServer.LOGGER.warn("SOMETHING EVERY WRONG! PLEASE REPORT THE EXCEPTION BELOW TO CARDBOARD:");
            e.printStackTrace();
            return null;
        }
    }

    /**
     */
    public static String getCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(ReflectionRemapper.class.getName()) && ste.getClassName().indexOf("java.lang.Thread")!=0)
                return ste.getClassName();
        }
        return null;
    }

    public static Method getMethodByName(Class<?> calling, String f, Class<?>[] p) throws ClassNotFoundException, NoSuchMethodException {
        Method m = getDeclaredMethodByName(calling, f, p);
        m.setAccessible(true);
        return m;
    }
}
