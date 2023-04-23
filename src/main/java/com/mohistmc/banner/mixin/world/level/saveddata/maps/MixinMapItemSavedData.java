package com.mohistmc.banner.mixin.world.level.saveddata.maps;

import com.mohistmc.banner.injection.world.level.saveddata.maps.InjectionMapItemSavedData;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.map.CraftMapView;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;

@Mixin(MapItemSavedData.class)
public class MixinMapItemSavedData implements InjectionMapItemSavedData {

    public CraftMapView mapView;
    private CraftServer server;
    public UUID uniqueId = null;
    public String id;

    @Override
    public CraftMapView bridge$mapView() {
        return mapView;
    }

    @Override
    public UUID bridge$uniqueId() {
        return uniqueId;
    }

    @Override
    public String bridge$id() {
        return id;
    }

    @Override
    public void banner$setUniqueId(UUID uuid) {
        this.uniqueId = uuid;
    }
}
