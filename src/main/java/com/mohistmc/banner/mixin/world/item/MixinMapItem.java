package com.mohistmc.banner.mixin.world.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.Bukkit;
import org.bukkit.event.server.MapInitializeEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MapItem.class)
public class MixinMapItem {

    /**
     * @author wdog5
     * @reason functionally replaced
     */
    @Overwrite
    @Nullable
    public static Integer getMapId(ItemStack stack) {
        CompoundTag compoundTag = stack.getTag();
        return compoundTag != null && compoundTag.contains("map", 99) ? compoundTag.getInt("map") : -1; // CraftBukkit - make new maps for no tag
    }

    @Inject(method = "createNewSavedData", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void banner$callMapEvent(Level level, int x, int z, int scale,
                                            boolean trackingPosition, boolean unlimitedTracking,
                                            ResourceKey<Level> dimension, CallbackInfoReturnable<Integer> cir,
                                            MapItemSavedData mapItemSavedData, int i) {
        // CraftBukkit start
        MapInitializeEvent event = new MapInitializeEvent(mapItemSavedData.bridge$mapView());
        Bukkit.getServer().getPluginManager().callEvent(event);
        // CraftBukkit end
    }
}
