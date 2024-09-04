package com.mohistmc.banner.bukkit;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SaplingBlock;
import org.bukkit.TreeType;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class BukkitFieldHooks {

    private static final VarHandle H_EVENT_FIRED;
    private static final VarHandle currentTick;
    private static final VarHandle treeType;

    static {
        try {
            var field = DispenserBlock.class.getDeclaredField("eventFired");
            H_EVENT_FIRED = MethodHandles.lookup().unreflectVarHandle(field);
            var currentTickField = MinecraftServer.class.getDeclaredField("currentTick");
            currentTick = MethodHandles.lookup().unreflectVarHandle(currentTickField);
            var treeTypeField = SaplingBlock.class.getDeclaredField("treeType");
            treeType = MethodHandles.lookup().unreflectVarHandle(treeTypeField);
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    public static int currentTick() {
        return (int) currentTick.get();
    }

    public static void setCurrentTick(int tick) {
        currentTick.set(tick);
    }

    public static boolean isEventFired() {
        return (boolean) H_EVENT_FIRED.get();
    }

    public static void setEventFired(boolean b) {
        H_EVENT_FIRED.set(b);
    }

    public static TreeType treeType() {
        return (TreeType) treeType.get();
    }

    public static void setTreeType(TreeType newType){
        treeType.set(newType);
    }
}
