package com.mohistmc.banner.bukkit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;

public class BukkitExtraConstants {

    public static final LootContextParam<Integer> LOOTING_MOD = new LootContextParam<>(new ResourceLocation("bukkit:looting_mod")); // CraftBukkit

    public static BlockState getBlockState(BlockState blockState, CompoundTag nbt) {
        StateDefinition<Block, BlockState> statecontainer = blockState.getBlock().getStateDefinition();
        for (String s : nbt.getAllKeys()) {
            Property<?> iproperty = statecontainer.getProperty(s);
            if (iproperty != null) {
                String s1 = nbt.get(s).getAsString();
                blockState = BlockItem.updateState(blockState, iproperty, s1);
            }
        }
        return blockState;
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
}
