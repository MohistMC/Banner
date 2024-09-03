package com.mohistmc.banner.translate.mixin;

import com.mohistmc.banner.BannerMCStart;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DedicatedServer.class)
public class MixinDedicatedServer {

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "Starting minecraft server version {}"))
    private String bosom$localStartingInfo(String constant) {
        return BannerMCStart.I18N.as("mc.server.starting");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "Loading properties"))
    private String bosom$localPropertiesInfo(String constant) {
        return BannerMCStart.I18N.as("mc.server.properties");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "Default game type: {}"))
    private String bosom$localGameType(String constant) {
        return BannerMCStart.I18N.as("mc.server.game_type");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "Starting Minecraft server on {}:{}"))
    private String bosom$localStartOn(String constant) {
        return BannerMCStart.I18N.as("mc.server.start_on");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "Done ({})! For help, type \"help\""))
    private String bosom$localStartDone(String constant) {
        return BannerMCStart.I18N.as("server.start.done");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!"))
    private String bosom$localStartWarn(String constant) {
        return BannerMCStart.I18N.as("server.warn.offline");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "The server will make no attempt to authenticate usernames. Beware."))
    private String bosom$localStartWarn0(String constant) {
        return BannerMCStart.I18N.as("server.warn.offline.0");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose."))
    private String bosom$localStartWarn1(String constant) {
        return BannerMCStart.I18N.as("server.warn.offline.1");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "To change this, set \"online-mode\" to \"true\" in the server.properties file."))
    private String bosom$localStartWarn2(String constant) {
        return BannerMCStart.I18N.as("server.warn.offline.2");
    }

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "Preparing level \"{}\""))
    private String bosom$localPrepareLevel(String constant) {
        return BannerMCStart.I18N.as("server.level.prepare");
    }
}