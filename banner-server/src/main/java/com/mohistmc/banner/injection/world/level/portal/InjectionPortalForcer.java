package com.mohistmc.banner.injection.world.level.portal;

import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;

public interface InjectionPortalForcer {

    default Optional<BlockUtil.FoundRectangle> findPortalAround(BlockPos pos, WorldBorder worldBorder, int searchRadius) {
        return Optional.empty();
    }

    default Optional<BlockUtil.FoundRectangle> createPortal(BlockPos pos, Direction.Axis axis, Entity entity, int createRadius) {
        return Optional.empty();
    }
}
