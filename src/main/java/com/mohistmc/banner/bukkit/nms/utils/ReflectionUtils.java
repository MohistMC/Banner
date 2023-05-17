package com.mohistmc.banner.bukkit.nms.utils;

import java.lang.reflect.Field;

@SuppressWarnings("removal")
public class ReflectionUtils {
    private static final SecurityManager securityManager = new SecurityManager();

    public static Class<?> getCallerClass(int skip) {
        return securityManager.getCallerClass(skip);
    }

    public static ClassLoader getCallerClassLoader() {
        return ReflectionUtils.getCallerClass(3).getClassLoader(); // added one due to it being the caller of the caller;
    }

    static class SecurityManager extends java.lang.SecurityManager {
        public Class<?> getCallerClass(int skip) {
            return getClassContext()[skip + 1];
        }
    }


    public static Field findField(Class<?> clazz, String name) {
        return findField(clazz, name, null);
    }


    public static Field findField(Class<?> clazz, String name, Class<?> type) {
        Class<?> searchType = clazz;
        while (!Object.class.equals(searchType) && searchType != null) {
            Field[] fields = searchType.getDeclaredFields();
            for (Field field : fields) {
                if ((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType()))) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }
}
