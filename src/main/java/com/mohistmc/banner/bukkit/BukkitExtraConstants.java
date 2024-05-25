package com.mohistmc.banner.bukkit;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.mohistmc.dynamicenum.MohistDynamEnum;
import com.mojang.datafixers.DSL;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.Unit;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.phys.AABB;
import org.bukkit.Bukkit;
import org.bukkit.TreeType;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;

public class BukkitExtraConstants {

    public static TreeType treeType; // CraftBukkit
    public static BlockPos openSign; // CraftBukkit
    public static int bridge$autosavePeriod;
    public static java.util.Queue<Runnable> bridge$processQueue =
            new java.util.concurrent.ConcurrentLinkedQueue<>();
    public static int currentTick = (int) (System.currentTimeMillis() / 50);
    public static boolean dispenser_eventFired = false; // CraftBukkit
    public static final TicketType<org.bukkit.plugin.Plugin> PLUGIN_TICKET =
            TicketType.create("plugin_ticket", Comparator.comparing(plugin -> plugin.getClass().getName())); // CraftBukkit
    public static final LootContextParam<Integer> LOOTING_MOD = new LootContextParam<>(new ResourceLocation("bukkit:looting_mod")); // CraftBukkit
    public static final TicketType<Unit> PLUGIN = TicketType.create("plugin", (a, b) -> 0); // CraftBukkit
    private static final DSL.TypeReference PDC_TYPE = () -> "bukkit_pdc";
    public static final DataFixTypes BUKKIT_PDC = MohistDynamEnum.addEnum(DataFixTypes.class, "BUKKIT_PDC", List.of(DSL.TypeReference.class), List.of(PDC_TYPE));

    public static List getHumansInRange(Level world, BlockPos blockposition, int i) {
        {
            double d0 = (double) (i * 10 + 10);

            AABB axisalignedbb = (new AABB(blockposition)).inflate(d0).expandTowards(0.0D, (double) world.getHeight(), 0.0D);
            List<Player> list = world.getEntitiesOfClass(Player.class, axisalignedbb);

            return list;
        }
    }

    public static FallingBlockEntity fall(Level level, BlockPos pos, BlockState blockState, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason spawnReason) {
        level.pushAddEntityReason(spawnReason);
        return FallingBlockEntity.fall(level, pos, blockState);
    }

    public static double a(int i) {
        return i % 32 == 0 ? 0.5D : 0.0D;
    }

    public static AABB recalculateBoundingBox(Entity entity, BlockPos blockPosition, Direction direction, int width, int height) {
        double d0 = blockPosition.getX() + 0.5;
        double d2 = blockPosition.getY() + 0.5;
        double d3 = blockPosition.getZ() + 0.5;
        double d4 = 0.46875;
        double d5 = a(width);
        double d6 = a(height);
        d0 -= direction.getStepX() * 0.46875;
        d3 -= direction.getStepZ() * 0.46875;
        d2 += d6;
        Direction enumdirection = direction.getCounterClockWise();
        d0 += d5 * enumdirection.getStepX();
        d3 += d5 * enumdirection.getStepZ();
        if (entity != null) {
            entity.setPosRaw(d0, d2, d3);
        }
        double d7 = width;
        double d8 = height;
        double d9 = width;
        if (direction.getAxis() == Direction.Axis.Z) {
            d9 = 1.0;
        } else {
            d7 = 1.0;
        }
        d7 /= 32.0;
        d8 /= 32.0;
        d9 /= 32.0;
        return new AABB(d0 - d7, d2 - d8, d3 - d9, d0 + d7, d2 + d8, d3 + d9);
    }

    public static InteractionResult applyBonemeal(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockPos blockPos2 = blockPos.relative(context.getClickedFace());
        if (BoneMealItem.growCrop(context.getItemInHand(), level, blockPos)) {
            if (!level.isClientSide) {
                level.levelEvent(1505, blockPos, 0);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            BlockState blockState = level.getBlockState(blockPos);
            boolean bl = blockState.isFaceSturdy(level, blockPos, context.getClickedFace());
            if (bl && BoneMealItem.growWaterPlant(context.getItemInHand(), level, blockPos2, context.getClickedFace())) {
                if (!level.isClientSide) {
                    level.levelEvent(1505, blockPos2, 0);
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    // CraftBukkit start
    public static void reload(MinecraftServer minecraftserver) {
        PackRepository resourcepackrepository = minecraftserver.getPackRepository();
        WorldData savedata = minecraftserver.getWorldData();
        Collection<String> collection = resourcepackrepository.getSelectedIds();
        Collection<String> collection1 = ReloadCommand.discoverNewPacks(resourcepackrepository, savedata, collection);
        minecraftserver.reloadResources(collection1);
    }

    public static MinecraftServer getServer() {
        return Bukkit.getServer() instanceof CraftServer ? ((CraftServer) Bukkit.getServer()).getServer() : null;
    }

    // Spigot start
    public static float range(float min, float value, float max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
    // Spigot end

    @Deprecated
    public static RegistryAccess getDefaultRegistryAccess() {
        return CraftRegistry.getMinecraftRegistry();
    }
    // CraftBukkit end
}
