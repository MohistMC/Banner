package com.mohistmc.banner.mixin.world.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.Bukkit;
import org.bukkit.event.server.MapInitializeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MapItem.class)
public class MixinMapItem {

    // Banner TODO Map ID

    @Inject(method = "createNewSavedData", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void banner$callMapEvent(Level level, int i, int j, int k, boolean bl, boolean bl2, ResourceKey<Level> resourceKey, CallbackInfoReturnable<MapId> cir, MapItemSavedData mapItemSavedData, MapId mapId) {
        // CraftBukkit start
        MapInitializeEvent event = new MapInitializeEvent(mapItemSavedData.bridge$mapView());
        Bukkit.getServer().getPluginManager().callEvent(event);
        // CraftBukkit end
    }
}
