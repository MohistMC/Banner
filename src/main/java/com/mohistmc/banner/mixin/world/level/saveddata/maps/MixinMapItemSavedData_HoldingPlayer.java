package com.mohistmc.banner.mixin.world.level.saveddata.maps;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.map.RenderData;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapItemSavedData.HoldingPlayer.class)
public abstract class MixinMapItemSavedData_HoldingPlayer {

    @Shadow @Final private MapItemSavedData field_132;
    @Shadow @Final public Player player;
    @Unique
    private byte[] banner$colors = field_132.colors;
    @Unique
    private java.util.Collection<MapDecoration> icons = new java.util.ArrayList<>();

    private AtomicReference<RenderData> banner$render = new AtomicReference<>();

    @Inject(method = "nextUpdatePacket", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData$HoldingPlayer;createPatch()Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData$MapPatch;"))
    private void banner$checkColors(int mapId, CallbackInfoReturnable<Packet<?>> cir) {
        if (this.player != null) {
            RenderData render = field_132.bridge$mapView().render((CraftPlayer) this.player.getBukkitEntity()); // CraftBukkit
            banner$render.set(render);
            field_132.colors = render.buffer;
        }
    }

    @Inject(method = "nextUpdatePacket", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData$HoldingPlayer;createPatch()Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData$MapPatch;",
            shift = At.Shift.AFTER))
    private void banner$setColors(int mapId, CallbackInfoReturnable<Packet<?>> cir) {
        if (banner$colors != null) {
            field_132.colors = banner$colors;
        }
    }

    @Redirect(method = "nextUpdatePacket", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
    private Collection<MapDecoration> banner$resetCollections(Map instance) {
        if (this.player != null && banner$render.get() != null) {
            // CraftBukkit start
            for (org.bukkit.map.MapCursor cursor : banner$render.getAndSet(null).cursors) {
                if (cursor.isVisible()) {
                    icons.add(new MapDecoration(MapDecoration.Type.byIcon(cursor.getRawType()), cursor.getX(), cursor.getY(), cursor.getDirection(), CraftChatMessage.fromStringOrNull(cursor.getCaption())));
                }
            }
            return icons;
            // CraftBukkit end
        } else {
            return field_132.decorations.values();
        }
    }
}
