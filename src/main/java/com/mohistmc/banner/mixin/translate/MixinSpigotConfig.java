package com.mohistmc.banner.mixin.translate;

import com.mohistmc.banner.BannerMCStart;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.spigotmc.SpigotConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.PrintStream;

@Mixin(value = SpigotConfig.class, remap = false)
public class MixinSpigotConfig {

    @Shadow public static int playerSample;

    @Shadow public static YamlConfiguration config;

    @Shadow public static String outdatedClientMessage;

    @Shadow public static String outdatedServerMessage;

    @Inject(method = "messages", at = @At("HEAD"))
    private static void bosom$resetHeader(CallbackInfo ci) {
        outdatedClientMessage = BannerMCStart.I18N.as("spigotconfig.19");
        outdatedServerMessage = BannerMCStart.I18N.as("spigotconfig.20");
    }

    @Redirect(method = "init", at = @At(value = "INVOKE",
            target = "Lorg/bukkit/configuration/file/YamlConfigurationOptions;header(Ljava/lang/String;)Lorg/bukkit/configuration/file/YamlConfigurationOptions;"))
    private static YamlConfigurationOptions bosom$resetHeader(YamlConfigurationOptions instance, String value) {
        String I18N_HEADER =
                BannerMCStart.I18N.as("spigotconfig.1") + "\n"
                        +  BannerMCStart.I18N.as("spigotconfig.2") + "\n"
                        +  BannerMCStart.I18N.as("spigotconfig.3") + "\n"
                        +  BannerMCStart.I18N.as("spigotconfig.4") + "\n"
                        + "http://www.spigotmc.org/wiki/spigot-configuration/\n"
                        + "\n"
                        +  BannerMCStart.I18N.as("spigotconfig.5") + "\n"
                        +  BannerMCStart.I18N.as("spigotconfig.6") + "\n"
                        + "\n"
                        + "Discord: https://www.spigotmc.org/go/discord\n"
                        + "Forums: http://www.spigotmc.org/\n";
        return config.options().header(I18N_HEADER);
    }

    @ModifyConstant(method = "init",
            constant = @Constant(stringValue = "Could not load spigot.yml, please correct your syntax errors"))
    private static String bosom$i18nSpigotConfig5(String constant) {
        return BannerMCStart.I18N.as("spigotconfig.7");
    }

    @ModifyConstant(method = "nettyThreads",
            constant = @Constant(stringValue = "Using {0} threads for Netty based IO"))
    private static String bosom$i18nSpigotConfig6(String constant) {
        return BannerMCStart.I18N.as("spigotconfig.8");
    }

    @ModifyConstant(method = "stats",
            constant = @Constant(stringValue = "Ignoring non existent stats.forced-stats "))
    private static String bosom$i18nSpigotConfig7(String constant) {
        return BannerMCStart.I18N.as("spigotconfig.10") + " ";
    }

    @ModifyConstant(method = "stats",
            constant = @Constant(stringValue = "Ignoring invalid stats.forced-stats "))
    private static String bosom$i18nSpigotConfig8(String constant) {
        return BannerMCStart.I18N.as("spigotconfig.9") + " ";
    }

    @Redirect(method = "playerSample", at = @At(value =
            "INVOKE", target = "Ljava/io/PrintStream;println(Ljava/lang/String;)V"))
    private static void bosom$i18nSpigotConfig9(PrintStream instance, String x) {
        System.out.println( BannerMCStart.I18N.as("spigotconfig.11") + " " + playerSample );
    }

    @ModifyConstant(method = "debug",
            constant = @Constant(stringValue = "Debug logging is enabled"))
    private static String bosom$i18nSpigotConfig10(String constant) {
        return BannerMCStart.I18N.as("spigotconfig.12");
    }

    @ModifyConstant(method = "debug",
            constant = @Constant(stringValue = "Debug logging is disabled"))
    private static String bosom$i18nSpigotConfig11(String constant) {
        return BannerMCStart.I18N.as("spigotconfig.13");
    }

    @ModifyConstant(method = "bungee",
            constant = @Constant(stringValue = "Outdated config, disabling BungeeCord support!"))
    private static String bosom$i18nSpigotConfig12(String constant) {
        return BannerMCStart.I18N.as("spigotconfig.14");
    }

    @ModifyConstant(method = "watchdog",
            constant = @Constant(stringValue = "Server is restarting"))
    private static String bosom$i18nSpigotConfig13(String constant) {
        return BannerMCStart.I18N.as("spigotconfig.15");
    }

    @ModifyConstant(method = "messages",
            constant = @Constant(stringValue = "You are not whitelisted on this server!"))
    private static String bosom$i18nSpigotConfig14(String constant) {
        return BannerMCStart.I18N.as("spigotconfig.16");
    }

    @ModifyConstant(method = "messages",
            constant = @Constant(stringValue = "Unknown command. Type \"/help\" for help."))
    private static String bosom$i18nSpigotConfig15(String constant) {
        return BannerMCStart.I18N.as("spigotconfig.17");
    }

    @ModifyConstant(method = "messages",
            constant = @Constant(stringValue = "The server is full!"))
    private static String bosom$i18nSpigotConfig16(String constant) {
        return BannerMCStart.I18N.as("spigotconfig.18");
    }

    @ModifyConstant(method = "readConfig",
            constant = @Constant(stringValue = "Could not save "))
    private static String bosom$i18nSpigotConfig19(String constant) {
        return BannerMCStart.I18N.as("spigotconfig.21") + " ";
    }

    @ModifyConstant(method = "readConfig",
            constant = @Constant(stringValue = "Error invoking "))
    private static String bosom$i18nSpigotConfig20(String constant) {
        return BannerMCStart.I18N.as("spigotconfig.22") + " ";
    }
}
