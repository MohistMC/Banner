package com.mohistmc.banner.mixin.world.level.portal;

import com.mohistmc.banner.injection.world.level.portal.InjectionPortalInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.portal.PortalInfo;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftPortalEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PortalInfo.class)
public class MixinPortalInfo implements InjectionPortalInfo {

    @Unique
    public ServerLevel world;
    @Unique
    public CraftPortalEvent portalEventInfo;

    @Override
    public void banner$setPortalEventInfo(CraftPortalEvent event) {
        this.portalEventInfo = event;
    }

    @Override
    public CraftPortalEvent bridge$getPortalEventInfo() {
        return this.portalEventInfo;
    }

    @Override
    public void banner$setWorld(ServerLevel world) {
        this.world = world;
    }

    @Override
    public @Nullable ServerLevel bridge$getWorld() {
        return this.world;
    }
}
