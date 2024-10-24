package com.mohistmc.banner.mixin.server.level;

import com.google.common.collect.Lists;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/level/ServerLevel$EntityCallbacks")
public class MixinServerLevel_EntityCallbacks {

    @Shadow(aliases = {"field_26936"}) private ServerLevel outerThis;

    @Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"))
    private void banner$valid(Entity entity, CallbackInfo ci) {
         entity.banner$setValid(true);
        // Paper start - Set origin location when the entity is being added to the world
        if (entity.getOriginVector() == null) {
            entity.setOrigin(entity.getBukkitEntity().getLocation());
        }
        // Default to current world if unknown, gross assumption but entities rarely change world
        if (entity.getOriginWorld() == null) {
            entity.setOrigin(entity.getOriginVector().toLocation(outerThis.getWorld()));
        }
        // Paper end
    }

    @Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void banner$entityAdd(Entity entity, CallbackInfo ci) {
        new com.destroystokyo.paper.event.entity.EntityAddToWorldEvent(entity.getBukkitEntity()).callEvent(); // Paper - fire while valid
   }

    @Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void banner$entityCleanup(Entity entity, CallbackInfo ci) {
        if (entity instanceof Player player) {
            for (ServerLevel serverLevel : outerThis.getServer().getAllLevels()) {
                DimensionDataStorage worldData = serverLevel.getDataStorage();
                for (Object o : worldData.cache.values()) {
                    if (o instanceof MapItemSavedData map) {
                        map.carriedByPlayers.remove(player);
                         map.carriedBy.removeIf(holdingPlayer -> holdingPlayer.player == entity);
                    }
                }
            }
        }
        if (entity.getBukkitEntity() instanceof InventoryHolder holder) {
            for (org.bukkit.entity.HumanEntity h : Lists.newArrayList(holder.getInventory().getViewers())) {
                h.closeInventory();
            }
        }
        new com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent(entity.getBukkitEntity()).callEvent(); // Paper - fire while valid
    }

    @Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"))
    private void banner$invalid(Entity entity, CallbackInfo ci) {
        entity.banner$setInWorld(true);
        entity.banner$setValid(false);
        if (!(entity instanceof ServerPlayer)) {
            for (var player : outerThis.players()) {
                ((ServerPlayer) player).getBukkitEntity().onEntityRemove(entity);
            }
        }
    }
}
