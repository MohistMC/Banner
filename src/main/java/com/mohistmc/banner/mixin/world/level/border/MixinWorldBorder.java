package com.mohistmc.banner.mixin.world.level.border;

import com.mohistmc.banner.injection.world.level.border.InjectionWorldBorder;
import java.util.List;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorder.class)
public abstract class MixinWorldBorder implements InjectionWorldBorder {

    @Shadow @Final private List<BorderChangeListener> listeners;

    @Shadow public abstract double getCenterX();

    @Shadow public abstract double getCenterZ();

    @Shadow public abstract double getSize();

    @Shadow public abstract void lerpSizeBetween(double oldSize, double newSize, long time);

    public net.minecraft.world.level.Level world; // CraftBukkit

    @Override
    public Level bridge$world() {
        return world;
    }

    @Inject(method = "setCenter", at = @At("HEAD"), cancellable = true)
    private void banner$borderEvent0(double x, double z, CallbackInfo ci) {
        // Paper start
        if (this.world != null) {
            io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent event = new io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent(world.getWorld(), world.getWorld().getWorldBorder(), new org.bukkit.Location(world.getWorld(), this.getCenterX(), 0, this.getCenterZ()), new org.bukkit.Location(world.getWorld(), x, 0, z));
            if (!event.callEvent()) ci.cancel();
            x = event.getNewCenter().getX();
            z = event.getNewCenter().getZ();
        }
        // Paper end
    }

    @Inject(method = "setSize", at = @At("HEAD"), cancellable = true)
    private void banner$borderEvent1(double size, CallbackInfo ci) {
        // Paper start
        if (this.world != null) {
            io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent event = new io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent(world.getWorld(), world.getWorld().getWorldBorder(), io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent.Type.INSTANT_MOVE, getSize(), size, 0);
            if (!event.callEvent()) ci.cancel();
            if (event.getType() == io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent.Type.STARTED_MOVE && event.getDuration() > 0) { // If changed to a timed transition
                lerpSizeBetween(event.getOldSize(), event.getNewSize(), event.getDuration());
                ci.cancel();
            }
            size = event.getNewSize();
        }
        // Paper end
    }

    @Inject(method = "lerpSizeBetween", at = @At("HEAD"), cancellable = true)
    private void banner$borderEvent2(double fromSize, double toSize, long time, CallbackInfo ci) {
        // Paper start
        if (this.world != null) {
            io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent.Type type;
            if (fromSize == toSize) { // new size = old size
                type = io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent.Type.INSTANT_MOVE; // Use INSTANT_MOVE because below it creates a Static border if they are equal.
            } else {
                type = io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent.Type.STARTED_MOVE;
            }
            io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent event = new io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent(world.getWorld(), world.getWorld().getWorldBorder(), type, fromSize, toSize, time);
            if (!event.callEvent()) ci.cancel();
            toSize = event.getNewSize();
            time = event.getDuration();
        }
        // Paper end
    }

    @Override
    public void banner$setWorld(Level world) {
        this.world = world;
    }
}
