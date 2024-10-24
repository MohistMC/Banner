package com.mohistmc.banner.bukkit;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class DistValidate {

    private static final Marker MARKER = MarkerManager.getMarker("EXT_LOGIC");

    public static boolean isValid(UseOnContext context) {
        return context != null && isValid(context.getLevel());
    }

    public static boolean isValid(LevelAccessor level) {
        return level != null && level.getClass() == ServerLevel.class;
    }

    public static boolean isValid(BlockGetter getter) {
        return getter instanceof LevelAccessor level && isValid(level);
    }
}