package com.mohistmc.banner.mixin.server.dedicated;

import net.minecraft.server.dedicated.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.Properties;

@Mixin(Settings.class)
public class MixinSettings {

    @Inject(method = "loadFromFile",
            at = @At(value = "INVOKE",
            target = "Ljava/nio/file/Files;newInputStream(Ljava/nio/file/Path;[Ljava/nio/file/OpenOption;)Ljava/io/InputStream;"),
            cancellable = true)
    private static void banner$handleLoad(Path path, CallbackInfoReturnable<Properties> cir) {
        // CraftBukkit start - SPIGOT-7465, MC-264979: Don't load if file doesn't exist
        if (!path.toFile().exists()) {
            cir.setReturnValue(new Properties());
        }
        // CraftBukkit end
    }
}
