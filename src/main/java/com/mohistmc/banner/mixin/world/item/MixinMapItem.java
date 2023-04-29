package com.mohistmc.banner.mixin.world.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.Bukkit;
import org.bukkit.event.server.MapInitializeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapItem.class)
public class MixinMapItem {

    @ModifyReturnValue(method = "getMapId", at = @At("RETURN"))
    private static Integer banner$clearMapId(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        CompoundTag banner$compoundTag = stack.getTag();
        return banner$compoundTag != null && banner$compoundTag.contains("map", 99) ? banner$compoundTag.getInt("map") : -1; // CraftBukkit - make new maps for no tag
    }

    @Inject(method = "createNewSavedData", at = @At("RETURN"))
    private static void banner$callMapEvent(Level level, int x, int z, int scale, boolean trackingPosition, boolean unlimitedTracking, ResourceKey<Level> dimension, CallbackInfoReturnable<Integer> cir) {
        // CraftBukkit start
        MapInitializeEvent event = new MapInitializeEvent(MapItemSavedData.createFresh((double)x, (double)z, (byte)scale, trackingPosition, unlimitedTracking, dimension).bridge$mapView());
        Bukkit.getServer().getPluginManager().callEvent(event);
        // CraftBukkit end
    }
}
