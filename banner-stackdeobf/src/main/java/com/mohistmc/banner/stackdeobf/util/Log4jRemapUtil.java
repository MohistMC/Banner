package com.mohistmc.banner.stackdeobf.util;

import com.mohistmc.banner.stackdeobf.mappings.RemappingUtil;
import java.lang.reflect.Field;
import org.apache.logging.log4j.core.impl.ExtendedStackTraceElement;
import org.apache.logging.log4j.core.impl.ThrowableProxy;

final class Log4jRemapUtil {

    private static final Field PROXY_NAME = getField(ThrowableProxy.class, "name");
    private static final Field PROXY_MESSAGE = getField(ThrowableProxy.class, "message");
    private static final Field PROXY_LOCALIZED_MESSAGE = getField(ThrowableProxy.class, "localizedMessage");
    private static final Field EXT_STACK_ELEMENT = getField(ExtendedStackTraceElement.class, "stackTraceElement");

    private static Field getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    static void remapThrowableProxy(ThrowableProxy proxy) throws IllegalAccessException {
        // remap throwable classname
        if (proxy.getName() != null && proxy.getName().startsWith("net.minecraft.class_")) {
            PROXY_NAME.set(proxy, RemappingUtil.remapClasses(proxy.getName()));
        }

        // remap throwable message
        if (proxy.getMessage() != null) {
            PROXY_MESSAGE.set(proxy, RemappingUtil.remapString(proxy.getMessage()));
        }
        if (proxy.getLocalizedMessage() != null) {
            PROXY_LOCALIZED_MESSAGE.set(proxy, RemappingUtil.remapString(proxy.getLocalizedMessage()));
        }

        // remap throwable stack trace
        for (ExtendedStackTraceElement extElement : proxy.getExtendedStackTrace()) {
            StackTraceElement element = extElement.getStackTraceElement();
            element = RemappingUtil.remapStackTraceElement(element);
            EXT_STACK_ELEMENT.set(extElement, element);
        }

        // remap cause + suppressed throwables
        if (proxy.getCauseProxy() != null) {
            remapThrowableProxy(proxy.getCauseProxy());
        }
        for (ThrowableProxy suppressed : proxy.getSuppressedProxies()) {
            remapThrowableProxy(suppressed);
        }
    }
}
