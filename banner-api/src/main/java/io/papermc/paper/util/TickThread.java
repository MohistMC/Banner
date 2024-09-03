package io.papermc.paper.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;

import java.util.concurrent.atomic.AtomicInteger;

public final class TickThread extends Thread {

    public static final boolean STRICT_THREAD_CHECKS = Boolean.getBoolean("paper.strict-thread-checks");

    static {
        if (STRICT_THREAD_CHECKS) {
            MinecraftServer.LOGGER.warn("Strict thread checks enabled - performance may suffer");
        }
    }

    public static void softEnsureTickThread(final String reason) {
        if (!STRICT_THREAD_CHECKS) {
            return;
        }
        ensureTickThread(reason);
    }

    public static void ensureTickThread(final String reason) {
        if (!isTickThread()) {
            MinecraftServer.LOGGER.error("Thread " + Thread.currentThread().getName() + " failed main thread check: " + reason, new Throwable());
            throw new IllegalStateException(reason);
        }
    }

    public static void ensureTickThread(final ServerLevel world, final int chunkX, final int chunkZ, final String reason) {
        if (!isTickThreadFor(world, chunkX, chunkZ)) {
            MinecraftServer.LOGGER.error("Thread " + Thread.currentThread().getName()  +" failed main thread check: " + reason, new Throwable());
            throw new IllegalStateException(reason);
        }
    }

    public static void ensureTickThread(final Entity entity, final String reason) {
        if (!isTickThreadFor(entity)) {
            MinecraftServer.LOGGER.error("Thread " + Thread.currentThread().getName() + " failed main thread check: " + reason, new Throwable());
            throw new IllegalStateException(reason);
        }
    }

    public final int id; /* We don't override getId as the spec requires that it be unique (with respect to all other threads) */

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    public TickThread(final String name) {
        this(null, name);
    }

    public TickThread(final Runnable run, final String name) {
        this(run, name, ID_GENERATOR.incrementAndGet());
    }

    private TickThread(final Runnable run, final String name, final int id) {
        super(run, name);
        this.id = id;
    }

    public static TickThread getCurrentTickThread() {
        return (TickThread)Thread.currentThread();
    }

    public static boolean isTickThread() {
        return Bukkit.isPrimaryThread();
    }

    public static boolean isTickThreadFor(final ServerLevel world, final int chunkX, final int chunkZ) {
        return Bukkit.isPrimaryThread();
    }

    public static boolean isTickThreadFor(final Entity entity) {
        return Bukkit.isPrimaryThread();
    }
}