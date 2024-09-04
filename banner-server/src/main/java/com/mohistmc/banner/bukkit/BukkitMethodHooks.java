package com.mohistmc.banner.bukkit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class BukkitMethodHooks {

    private static final MethodHandle zombifyVillager;
    private static final MethodHandle defaultRegistryAccess;
    private static final MethodHandle cbServer;
    private static final MethodHandle calculateBoundingBoxStaticItemFrame;
    private static final MethodHandle calculateBoundingBoxStaticPainting;

    static {
        try {
            var zombifyVillagerMethod = ZombieVillager.class.getDeclaredMethod("zombifyVillager", ServerLevel.class, Villager.class, BlockPos.class, boolean.class, CreatureSpawnEvent.SpawnReason.class);
            zombifyVillager = MethodHandles.lookup().unreflect(zombifyVillagerMethod);
            var defaultRegistryAccessMethod = MinecraftServer.class.getDeclaredMethod("getDefaultRegistryAccess");
            defaultRegistryAccess = MethodHandles.lookup().unreflect(defaultRegistryAccessMethod);
            var cbServerMethod = MinecraftServer.class.getDeclaredMethod("getServer");
            cbServer = MethodHandles.lookup().unreflect(cbServerMethod);
            var calculateBoundingBoxStaticItemFrameMethod = ItemFrame.class.getDeclaredMethod("calculateBoundingBoxStatic", BlockPos.class, Direction.class);
            calculateBoundingBoxStaticItemFrame = MethodHandles.lookup().unreflect(calculateBoundingBoxStaticItemFrameMethod);
            var calculateBoundingBoxStaticPaintingMethod = Painting.class.getDeclaredMethod("calculateBoundingBoxStatic", BlockPos.class, Direction.class, int.class, int.class);
            calculateBoundingBoxStaticPainting = MethodHandles.lookup().unreflect(calculateBoundingBoxStaticPaintingMethod);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static ZombieVillager zombifyVillager(ServerLevel level, Villager villager, BlockPos blockPosition, boolean silent, CreatureSpawnEvent.SpawnReason spawnReason) {
        try {
            return (ZombieVillager) zombifyVillager.invokeWithArguments(level, villager, blockPosition, silent, spawnReason);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static RegistryAccess getDefaultRegistryAccess() {
        try {
            return (RegistryAccess) defaultRegistryAccess.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static MinecraftServer getServer() {
        try {
            return (MinecraftServer) cbServer.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static AABB calculateBoundingBoxStaticItemFrame(BlockPos blockposition, Direction enumdirection) {
        try {
            return (AABB) calculateBoundingBoxStaticItemFrame.invokeWithArguments(blockposition, enumdirection);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static AABB calculateBoundingBoxStaticPainting(BlockPos blockposition, Direction enumdirection, int width, int height) {
        try {
            return (AABB) calculateBoundingBoxStaticPainting.invokeWithArguments(blockposition, enumdirection, width, height);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
