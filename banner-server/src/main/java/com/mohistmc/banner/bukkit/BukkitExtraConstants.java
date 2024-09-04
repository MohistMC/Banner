package com.mohistmc.banner.bukkit;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.phys.AABB;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class BukkitExtraConstants {

    public static BlockPos openSign; // CraftBukkit
    public static int bridge$autosavePeriod;
    public static java.util.Queue<Runnable> bridge$processQueue =
            new java.util.concurrent.ConcurrentLinkedQueue<>();
    public static final TicketType<org.bukkit.plugin.Plugin> PLUGIN_TICKET =
            TicketType.create("plugin_ticket", Comparator.comparing(plugin -> plugin.getClass().getName())); // CraftBukkit
    public static final LootContextParam<Integer> LOOTING_MOD = new LootContextParam<>(ResourceLocation.parse("bukkit:looting_mod")); // CraftBukkit
    public static final TicketType<Unit> PLUGIN = TicketType.create("plugin", (a, b) -> 0); // CraftBukkit

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

    public static int getRange(List<BlockPos> list) {
        int i = list.size();
        int j = i / 7 * 16;
        return j;
    }
}
