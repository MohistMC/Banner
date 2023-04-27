package com.mohistmc.banner.mixin.world.level.border;

import com.mohistmc.banner.injection.world.level.border.InjectionWorldBorder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WorldBorder.class)
public class MixinWorldBorder implements InjectionWorldBorder {

    public net.minecraft.server.level.ServerLevel world; // CraftBukkit

    @Override
    public ServerLevel bridge$world() {
        return world;
    }

    @Override
    public void banner$setWorld(ServerLevel world) {
        this.world = world;
    }
}
