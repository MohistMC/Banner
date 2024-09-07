package com.mohistmc.banner.bukkit_manager.mixin;

import com.mohistmc.banner.fabric.FabricEventFactory;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RegisteredListener.class, remap = false)
public class MixinRegisteredListener {

    @Inject(method = "callEvent", at = @At(value = "INVOKE",
            target = "Lorg/bukkit/plugin/EventExecutor;execute(Lorg/bukkit/event/Listener;Lorg/bukkit/event/Event;)V"))
    private void banner$hookEvent(Event event, CallbackInfo ci) {
        if (Bukkit.getServer() != null) {
            FabricEventFactory.HOOK_BUKKIT.invoker().hook(event);
        }
    }
}
