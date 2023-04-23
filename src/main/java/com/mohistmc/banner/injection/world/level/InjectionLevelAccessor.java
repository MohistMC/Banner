package com.mohistmc.banner.injection.world.level;

public interface InjectionLevelAccessor {
    default net.minecraft.server.level.ServerLevel getMinecraftWorld() {
        return null;
    }
}
