package com.mohistmc.banner.bukkit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

public class BukkitMethodHooks {

    private static final MethodHandle zombifyVillager;
    private static final MethodHandle defaultRegistryAccess;
    private static final MethodHandle cbServer;
    private static final MethodHandle calculateBoundingBoxStaticItemFrame;
    private static final MethodHandle calculateBoundingBoxStaticPainting;
    private static final MethodHandle humansInRange;
    private static final MethodHandle range;
    private static final MethodHandle conduitRange;
    private static final MethodHandle fall;
    private static final MethodHandle applyBonemeal;
    private static final MethodHandle reload;

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
            var humansInRangeMethod = BeaconBlockEntity.class.getDeclaredMethod("getHumansInRange", Level.class, BlockPos.class, int.class);
            humansInRange = MethodHandles.lookup().unreflect(humansInRangeMethod);
            var rangeMethod = Block.class.getDeclaredMethod("range", float.class, float.class, float.class);
            range = MethodHandles.lookup().unreflect(rangeMethod);
            var conduitRangeMethod = ConduitBlockEntity.class.getDeclaredMethod("getRange", List.class);
            conduitRange = MethodHandles.lookup().unreflect(conduitRangeMethod);
            var fallMethod = FallingBlockEntity.class.getDeclaredMethod("fall", Level.class, BlockPos.class, BlockState.class, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.class);
            fall = MethodHandles.lookup().unreflect(fallMethod);
            var applyBonemealMethod = BoneMealItem.class.getDeclaredMethod("applyBonemeal", UseOnContext.class);
            applyBonemeal = MethodHandles.lookup().unreflect(applyBonemealMethod);
            var reloadMethod = ReloadCommand.class.getDeclaredMethod("reload", MinecraftServer.class);
            reload = MethodHandles.lookup().unreflect(reloadMethod);
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

    public static List getHumansInRange(Level world, BlockPos blockposition, int i) {
        try {
            return (List) humansInRange.invokeWithArguments(world, blockposition, i);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static float range(float min, float value, float max) {
        try {
            return (float) range.invokeWithArguments(min, value, max);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int getRange(List<BlockPos> list) {
        try {
            return (int) conduitRange.invokeWithArguments(list);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static FallingBlockEntity fall(Level level, BlockPos pos, BlockState blockState, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason spawnReason) {
        try {
            return (FallingBlockEntity) fall.invokeWithArguments(level, pos, blockState, spawnReason);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static InteractionResult applyBonemeal(UseOnContext context) {
        try {
            return (InteractionResult) applyBonemeal.invokeWithArguments(context);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static MinecraftServer reload(MinecraftServer minecraftserver) {
        try {
            return (MinecraftServer) reload.invokeWithArguments(minecraftserver);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
