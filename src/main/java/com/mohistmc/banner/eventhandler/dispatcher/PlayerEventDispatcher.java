package com.mohistmc.banner.eventhandler.dispatcher;

import com.mohistmc.banner.bukkit.BukkitCaptures;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

public class PlayerEventDispatcher {

    public static void dispatcherPlayer() {
        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            if (!entity.getLevel().dimensionType().bedWorks()) {
                explodeBed(entity.getBlockStateOn(), entity.getLevel(), entity.getOnPos());
            }
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            ItemStack heldStack = player.getUseItem();
            if (!CraftEventFactory.handlePlayerShearEntityEvent(player, entity, heldStack, hand)) {
                return InteractionResult.PASS;
            }
            return InteractionResult.PASS;
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BukkitCaptures.capturePlaceEventHand(hand);
            BukkitCaptures.getPlaceEventHand(InteractionHand.MAIN_HAND);
            return InteractionResult.PASS;
        });
    }

    private static void handleBlockDrop(BukkitCaptures.BlockBreakEventContext breakEventContext, BlockPos pos, Level world, ServerPlayer player) {
        BlockBreakEvent breakEvent = breakEventContext.getEvent();
        List<ItemEntity> blockDrops = breakEventContext.getBlockDrops();
        org.bukkit.block.BlockState state = breakEventContext.getBlockBreakPlayerState();

        if (blockDrops != null && (breakEvent == null || breakEvent.isDropItems())) {
            CraftBlock craftBlock = CraftBlock.at(world, pos);
            CraftEventFactory.handleBlockDropItemEvent(craftBlock, state, player, blockDrops);
        }
    }

    // CraftBukkit start
    private static void explodeBed(BlockState iblockdata, Level world, BlockPos blockposition) {
        {
            {
                world.removeBlock(blockposition, false);
                BlockPos blockposition1 = blockposition.relative((Direction) (iblockdata.getValue(BedBlock.FACING)).getOpposite());

                if (world.getBlockState(blockposition1).getBlock() instanceof BedBlock) {
                    world.removeBlock(blockposition1, false);
                }

                Vec3 vec3d = blockposition.getCenter();

                world.explode((Entity) null, world.damageSources().badRespawnPointExplosion(vec3d), (ExplosionDamageCalculator) null, vec3d, 5.0F, true, Level.ExplosionInteraction.BLOCK);
            }
        }
    }
    // CraftBukkit end

}
