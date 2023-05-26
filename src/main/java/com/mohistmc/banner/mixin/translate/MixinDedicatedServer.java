package com.mohistmc.banner.mixin.translate;

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

    @ModifyConstant(method = "initServer", constant = @Constant(stringValue = "Starting Minecraft server on {}:{}"))
    private String banner$localStartDone(String constant) {
        return BannerMCStart.I18N.get("server.start.done");
    }
}
