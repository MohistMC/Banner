package com.mohistmc.banner.mixin.world.level.saveddata.maps;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.map.CraftMapCursor;
import org.bukkit.craftbukkit.map.RenderData;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(MapItemSavedData.HoldingPlayer.class)
public abstract class MixinMapItemSavedData_HoldingPlayer {

    @Shadow @Final private MapItemSavedData field_132;
    @Shadow @Final public Player player;
    @Unique
    private byte[] banner$colors = field_132.colors;
    @Unique
    private java.util.Collection<MapDecoration> icons = new java.util.ArrayList<>();

    private AtomicReference<RenderData> banner$render = new AtomicReference<>();
    private AtomicReference<Player> banner$player = new AtomicReference<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$initRender(MapItemSavedData mapItemSavedData, Player player, CallbackInfo ci) {
        banner$player.set(player);
    }

    @Inject(method = "nextUpdatePacket", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData$HoldingPlayer;createPatch()Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData$MapPatch;"))
    private void banner$checkColors(MapId mapId, CallbackInfoReturnable<Packet<?>> cir) {
        RenderData render = field_132.bridge$mapView().render((CraftPlayer) this.banner$player.getAndSet(null).getBukkitEntity()); // CraftBukkit
        banner$render.set(render);
        field_132.colors = render.buffer;
    }

    @Inject(method = "nextUpdatePacket", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData$HoldingPlayer;createPatch()Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData$MapPatch;",
            shift = At.Shift.AFTER))
    private void banner$setColors(MapId mapId, CallbackInfoReturnable<Packet<?>> cir) {
        field_132.colors = banner$colors;
    }

    @Redirect(method = "nextUpdatePacket", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
    private Collection<MapDecoration> banner$resetCollections(Map instance) {
        // CraftBukkit start
        for (org.bukkit.map.MapCursor cursor : banner$render.getAndSet(null).cursors) {
            if (cursor.isVisible()) {
                icons.add(new MapDecoration(CraftMapCursor.CraftType.bukkitToMinecraftHolder(cursor.getType()), cursor.getX(), cursor.getY(), cursor.getDirection(), CraftChatMessage.fromStringOrOptional(cursor.getCaption())));
            }
        }
        return icons;
        // CraftBukkit end
    }
}