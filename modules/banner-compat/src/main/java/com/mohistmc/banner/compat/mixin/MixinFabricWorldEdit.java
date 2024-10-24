package com.mohistmc.banner.compat.mixin;

import com.sk89q.worldedit.fabric.FabricWorldEdit;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.sk89q.worldedit.fabric.FabricWorldEdit")
public abstract class MixinFabricWorldEdit {

    @Shadow protected abstract void setupRegistries(MinecraftServer server);

    @Shadow public static FabricWorldEdit inst;

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/event/Event;register(Ljava/lang/Object;)V", ordinal = 0))
    private static <T> void banner$compatWorldEdit(Event<T> instance, T t) {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ((MixinFabricWorldEdit) (Object) inst).setupRegistries(server);
            ((ServerLifecycleEvents.ServerStarted) t).onServerStarted(server);
        });
    }
}
