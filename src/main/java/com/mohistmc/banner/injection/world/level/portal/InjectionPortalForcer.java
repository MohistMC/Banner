package com.mohistmc.banner.injection.world.level.portal;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;

import java.util.Optional;

public interface InjectionPortalForcer {

    default Optional<BlockUtil.FoundRectangle> findPortalAround(BlockPos pos, WorldBorder worldBorder, int searchRadius) {
        return Optional.empty();
    }

    default Optional<BlockUtil.FoundRectangle> createPortal(BlockPos pos, Direction.Axis axis, Entity entity, int createRadius) {
        return Optional.empty();
    }
}
