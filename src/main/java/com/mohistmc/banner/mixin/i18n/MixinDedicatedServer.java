package com.mohistmc.banner.mixin.i18n;

import com.mohistmc.banner.BannerMCStart;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DedicatedServer.class)
public class MixinDedicatedServer {

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "Starting minecraft server version {}"))
    private String banner$localStartingInfo(String constant) {
        return BannerMCStart.I18N.get("mc.server.starting");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "Loading properties"))
    private String banner$localPropertiesInfo(String constant) {
        return BannerMCStart.I18N.get("mc.server.properties");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "Default game type: {}"))
    private String banner$localGameType(String constant) {
        return BannerMCStart.I18N.get("mc.server.game_type");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "Starting Minecraft server on {}:{}"))
    private String banner$localStartOn(String constant) {
        return BannerMCStart.I18N.get("mc.server.start_on");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "Done ({})! For help, type \"help\""))
    private String banner$localStartDone(String constant) {
        return BannerMCStart.I18N.get("server.start.done");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!"))
    private String banner$localStartWarn(String constant) {
        return BannerMCStart.I18N.get("server.warn.offline");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "The server will make no attempt to authenticate usernames. Beware."))
    private String banner$localStartWarn0(String constant) {
        return BannerMCStart.I18N.get("server.warn.offline.0");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose."))
    private String banner$localStartWarn1(String constant) {
        return BannerMCStart.I18N.get("server.warn.offline.1");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "To change this, set \"online-mode\" to \"true\" in the server.properties file."))
    private String banner$localStartWarn2(String constant) {
        return BannerMCStart.I18N.get("server.warn.offline.2");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "Preparing level \"{}\""))
    private String banner$localPrepareLevel(String constant) {
        return BannerMCStart.I18N.get("server.level.prepare");
    }
}
