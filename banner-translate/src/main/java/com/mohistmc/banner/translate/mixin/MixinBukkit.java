package com.mohistmc.banner.translate.mixin;

import com.mohistmc.banner.BannerMCStart;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@Mixin(value = Bukkit.class, remap = false)
public abstract class MixinBukkit {

    @Shadow public static @NotNull String getName() {return null;}
    @Shadow public static @NotNull String getVersion() {return null;}
    @Shadow public static @NotNull String getBukkitVersion() {return null;}

    private static AtomicReference<Server> bosom$server = new AtomicReference<>();

    @Inject(method = "setServer", at = @At(value = "INVOKE",
            target = "Ljava/util/logging/Logger;info(Ljava/lang/String;)V"))
    private static void bosom$getServer(Server server, CallbackInfo ci) {
        bosom$server.set(server);
    }

    @Redirect(method = "setServer", at = @At(value = "INVOKE",
            target = "Ljava/util/logging/Logger;info(Ljava/lang/String;)V"))
    private static void bosom$i18nInfo(Logger instance, String msg) {
        bosom$server.get().getLogger().info(
                BannerMCStart.I18N.as("bukkit.version.servername") + " "
                        + getName() + " "
                        + BannerMCStart.I18N.as("bukkit.version.version") + " "
                        + getVersion() + " "
                        + BannerMCStart.I18N.as("bukkit.version.apiversion") + " "
                        + getBukkitVersion() + ")");
    }
}

