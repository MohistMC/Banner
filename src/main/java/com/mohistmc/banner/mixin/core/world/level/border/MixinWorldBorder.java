package com.mohistmc.banner.mixin.core.world.level.border;

import com.mohistmc.banner.injection.world.level.border.InjectionWorldBorder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WorldBorder.class)
public class MixinWorldBorder implements InjectionWorldBorder {

    @Shadow @Final private List<BorderChangeListener> listeners;
    public net.minecraft.server.level.ServerLevel world; // CraftBukkit

    @Override
    public ServerLevel bridge$world() {
        return world;
    }

    @Inject(method = "addListener", at = @At("HEAD"), cancellable = true)
    private void banner$checkBorder(BorderChangeListener listener, CallbackInfo ci) {
        if (listeners.contains(listener)) ci.cancel(); // CraftBukkit
    }

    @Override
    public void banner$setWorld(ServerLevel world) {
        this.world = world;
    }
}
