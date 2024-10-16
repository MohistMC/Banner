package com.mohistmc.banner.stackdeobf.mappings;

// Created by booky10 in StackDeobfuscator (17:43 17.12.22)

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RemappingUtil {

    static final Pattern CLASS_PATTERN = Pattern.compile("(net\\.minecraft\\.|net/minecraft/)?class_(\\d+)");
    private static final Pattern METHOD_PATTERN = Pattern.compile("method_(\\d+)");
    private static final Pattern FIELD_PATTERN = Pattern.compile("field_(\\d+)");

    private RemappingUtil() {
    }

    public static String remapClasses(String string) {
        return CLASS_PATTERN.matcher(string).replaceAll(result -> {
            int classId = Integer.parseInt(result.group(2));
            String className = CachedMappings.remapClass(classId);
            if (className == null) {
                return result.group();
            }

            String packageName = result.group(1);
            if (packageName != null) {
                // a package has been specified, don't remove it
                if (packageName.indexOf('.') == -1) {
                    // original package name contains "/" as package separator instead of "."
                    return className.replace('.', '/');
                }
                return className;
            }

            // no package in original string, remove it
            int packageIndex = className.lastIndexOf('.');
            if (packageIndex != -1) {
                className = className.substring(packageIndex + 1);
            }
            return className;
        });
    }

    public static String remapMethods(String string) {
        return METHOD_PATTERN.matcher(string).replaceAll(result -> {
            int methodId = Integer.parseInt(result.group(1));
            String methodName = CachedMappings.remapMethod(methodId);
            return methodName == null ? result.group() : Matcher.quoteReplacement(methodName);
        });
    }

    public static String remapFields(String string) {
        return FIELD_PATTERN.matcher(string).replaceAll(result -> {
            int fieldId = Integer.parseInt(result.group(1));
            String fieldName = CachedMappings.remapField(fieldId);
            return fieldName == null ? result.group() : fieldName;
        });
    }

    public static String remapString(String string) {
        if (string.contains("class_")) {
            string = remapClasses(string);
        }

        if (string.contains("method_")) {
            string = remapMethods(string);
        }

        if (string.contains("field_")) {
            string = remapFields(string);
        }

        return string;
    }

    public static Throwable remapThrowable(Throwable throwable) {
        if (throwable instanceof RemappedThrowable) {
            return throwable;
        }

        StackTraceElement[] stackTrace = throwable.getStackTrace();
        remapStackTraceElements(stackTrace);

        Throwable cause = throwable.getCause();
        if (cause != null) {
            cause = remapThrowable(cause);
        }

        String message = throwable.getMessage();
        if (message != null) {
            message = remapString(message);
        }

        String throwableName = throwable.getClass().getName();
        if (throwableName.startsWith("net.minecraft.class_")) {
            throwableName = remapClasses(throwableName);
        }

        Throwable remapped = new RemappedThrowable(message, cause, throwable, throwableName);
        remapped.setStackTrace(stackTrace);
        for (Throwable suppressed : throwable.getSuppressed()) {
            remapped.addSuppressed(remapThrowable(suppressed));
        }
        return remapped;
    }

    public static void remapStackTraceElements(StackTraceElement[] elements) {
        for (int i = 0; i < elements.length; i++) {
            elements[i] = remapStackTraceElement(elements[i]);
        }
    }

    public static StackTraceElement remapStackTraceElement(StackTraceElement element) {
        String className = element.getClassName();
        boolean remappedClass = false;
        if (className.startsWith("net.minecraft.class_")) {
            className = remapClasses(className);
            remappedClass = true;
        }

        String fileName = element.getFileName();
        if (fileName != null && fileName.startsWith("class_")) {
            fileName = remapClasses(fileName);
        }

        String methodName = element.getMethodName();
        if (methodName.startsWith("method_")) {
            methodName = remapMethods(methodName);
        }

        String classLoaderName = element.getClassLoaderName();
        if (remappedClass) {
            if (classLoaderName == null) {
                classLoaderName = "MC";
            } else {
                classLoaderName += "//MC";
            }
        }

        return new StackTraceElement(classLoaderName, element.getModuleName(), element.getModuleVersion(),
                className, methodName, fileName, element.getLineNumber());
    }
}

