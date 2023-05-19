package com.mohistmc.banner.eventhandler.dispatcher;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class BlockEventDispatcher {

    private static AtomicReference<BlockBreakEvent> breakEvent = new AtomicReference<>();

    public static void dispatchBlockEvents() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            world.banner$setCaptureDrops(new ArrayList<>());
            // CraftBukkit start - fire BlockBreakEvent
            org.bukkit.block.Block bblock = CraftBlock.at(world, pos);
            BlockBreakEvent event = null;
            if (player instanceof ServerPlayer serverPlayer) {
                // Sword + Creative mode pre-cancel
                boolean isSwordNoBreak = !serverPlayer.getMainHandItem().getItem().canAttackBlock(state, world, pos, serverPlayer);

                // Tell client the block is gone immediately then process events
                // Don't tell the client if its a creative sword break because its not broken!
                if (world.getBlockEntity(pos) == null && !isSwordNoBreak) {
                    ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(pos, Blocks.AIR.defaultBlockState());
                    serverPlayer.connection.send(packet);
                }

                event = new BlockBreakEvent(bblock, serverPlayer.getBukkitEntity());
                breakEvent.set(event);

                // Sword + Creative mode pre-cancel
                event.setCancelled(isSwordNoBreak);

                // Calculate default block experience
                BlockState nmsData = world.getBlockState(pos);
                Block nmsBlock = nmsData.getBlock();

                ItemStack itemstack = serverPlayer.getItemBySlot(EquipmentSlot.MAINHAND);

                if (nmsBlock != null && !event.isCancelled() && !serverPlayer.isCreative() && serverPlayer.hasCorrectToolForDrops(nmsBlock.defaultBlockState())) {
                    if (world instanceof ServerLevel serverLevel) {
                        event.setExpToDrop(nmsBlock.getExpDrop(nmsData, serverLevel, pos, itemstack, true));
                    }
                }

                world.getCraftServer().getPluginManager().callEvent(event);

                if (!event.isDropItems()) {
                    return false;
                }

                if (event.isCancelled()) {
                    if (isSwordNoBreak) {
                        return false;
                    }
                    // Let the client know the block still exists
                    serverPlayer.connection.send(new ClientboundBlockUpdatePacket(world, pos));

                    // Brute force all possible updates
                    for (Direction dir : Direction.values()) {
                        serverPlayer.connection.send(new ClientboundBlockUpdatePacket(world, pos.relative(dir)));
                    }

                    // Update any tile entity data for this block
                    BlockEntity tileentity = world.getBlockEntity(pos);
                    if (tileentity != null) {
                        serverPlayer.connection.send(tileentity.getUpdatePacket());
                    }
                    return false;
                }
            }
            return true;
        });
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            org.bukkit.block.BlockState bukkitState = CraftBlock.at(world, pos).getState();
            // CraftBukkit start
            if (breakEvent.get().isDropItems()) {
                CraftEventFactory.handleBlockDropItemEvent(CraftBlock.at(world, pos), bukkitState, (ServerPlayer) player, world.bridge$captureDrops());
            }

            world.banner$setCaptureDrops(null);

            boolean flag = world.removeBlock(pos, false);
            // Drop event experience
            if (flag && breakEvent.get() != null) {
                state.getBlock().popExperience((ServerLevel) world, pos, breakEvent.get().getExpToDrop());
            }
            // CraftBukkit end
        });
    }
}
