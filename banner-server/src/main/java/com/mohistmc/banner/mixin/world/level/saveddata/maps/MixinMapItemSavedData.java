package com.mohistmc.banner.mixin.world.level.saveddata.maps;

import com.mohistmc.banner.injection.world.level.saveddata.maps.InjectionMapItemSavedData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.map.CraftMapView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Mixin(MapItemSavedData.class)
public class MixinMapItemSavedData implements InjectionMapItemSavedData {

    @Shadow @Final public ResourceKey<Level> dimension;
    public CraftMapView mapView;
    private CraftServer server;
    public UUID uniqueId = null;
    public String id;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void banner$init(int i, int j, byte b, boolean bl, boolean bl2, boolean bl3, ResourceKey resourceKey, CallbackInfo ci) {
        this.mapView = new CraftMapView((MapItemSavedData) (Object) this);
        this.server = (CraftServer) Bukkit.getServer();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/Optional;orElseThrow(Ljava/util/function/Supplier;)Ljava/lang/Object;"))
    private static Object banner$customDimension(Optional<ResourceKey<Level>> optional, Supplier<?> exceptionSupplier, CompoundTag nbt) {
        return optional.orElseGet(() -> {
            long least = nbt.getLong("UUIDLeast");
            long most = nbt.getLong("UUIDMost");
            if (least != 0L && most != 0L) {
                UUID uniqueId = new UUID(most, least);
                CraftWorld world = (CraftWorld) Bukkit.getWorld(uniqueId);
                if (world != null) {
                    return world.getHandle().dimension();
                }
            }
            throw new IllegalArgumentException("Invalid map dimension: " + nbt.get("dimension"));
        });
    }

    @Inject(method = "save", at = @At("HEAD"))
    public void banner$storeDimension(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.uniqueId == null) {
            for (org.bukkit.World world : this.server.getWorlds()) {
                CraftWorld cWorld = (CraftWorld) world;
                if (cWorld.getHandle().dimension() != this.dimension) continue;
                this.uniqueId = cWorld.getUID();
                break;
            }
        }
        if (this.uniqueId != null) {
            compoundTag.putLong("UUIDLeast", this.uniqueId.getLeastSignificantBits());
            compoundTag.putLong("UUIDMost", this.uniqueId.getMostSignificantBits());
        }
    }

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

    public void banner$setId(String id) {
        this.id = id;
    }
}
