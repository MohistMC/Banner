package com.mohistmc.banner.eventhandler.dispatcher;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EntityEventDispatcher {

    public static void dispatchEntityEvent() {
        ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register((originalEntity, newEntity, origin, destination) -> {
            var pos = originalEntity.getOnPos();
            if (destination.getTypeKey() == LevelStem.NETHER) {
                originalEntity.callPortalEvent(originalEntity, destination, new Vec3(pos.getX(), pos.getY(), pos.getZ()),
                        PlayerTeleportEvent.TeleportCause.NETHER_PORTAL,
                        16, 16);
            }else if (destination.getTypeKey() == LevelStem.END) {
                if (Bukkit.getAllowEnd()) {
                    originalEntity.callPortalEvent(originalEntity, destination, new Vec3(pos.getX(), pos.getY(), pos.getZ()),
                            PlayerTeleportEvent.TeleportCause.END_PORTAL,
                            128, 16);
                }
            }else {
                originalEntity.callPortalEvent(originalEntity, destination, new Vec3(pos.getX(), pos.getY(), pos.getZ()),
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
