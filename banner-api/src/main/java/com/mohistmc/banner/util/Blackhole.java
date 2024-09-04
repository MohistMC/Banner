package com.mohistmc.banner.util;

import org.jetbrains.annotations.Contract;

public class Blackhole {

    public static void consume(Object o) {
    }

    @SuppressWarnings("Contract")
    @Contract("-> fail")
    public static boolean actuallyFalse() {
        return false;
    }

    @SuppressWarnings("Contract")
    @Contract("-> fail")
    public static <T> T nonConstant(T value) {
        return value;
    }
}
