package com.mohistmc.banner.eventhandler.dispatcher;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.core.PositionImpl;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.portal.PortalInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EntityEventDispatcher {

    public static void dispatchEntityEvent() {
        ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register((originalEntity, newEntity, origin, destination) -> {
            var pos = originalEntity.getOnPos();
            if (destination.getTypeKey() == LevelStem.NETHER) {
                originalEntity.callPortalEvent(originalEntity, destination, new PositionImpl(pos.getX(), pos.getY(), pos.getZ()),
                        PlayerTeleportEvent.TeleportCause.NETHER_PORTAL,
                        16, 16);
            }else if (destination.getTypeKey() == LevelStem.END) {
                originalEntity.callPortalEvent(originalEntity, destination, new PositionImpl(pos.getX(), pos.getY(), pos.getZ()),
                        PlayerTeleportEvent.TeleportCause.END_PORTAL,
                        128, 16);
            }else {
                originalEntity.callPortalEvent(originalEntity, destination, new PositionImpl(pos.getX(), pos.getY(), pos.getZ()),
                        PlayerTeleportEvent.TeleportCause.UNKNOWN,
                        0, 0);
            }
        });
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            // CraftBukkit start
            PlayerChangedWorldEvent changeEvent = new PlayerChangedWorldEvent(player.getBukkitEntity(), origin.getWorld());
            player.level().getCraftServer().getPluginManager().callEvent(changeEvent);
            // CraftBukkit end
        });
    }
}
