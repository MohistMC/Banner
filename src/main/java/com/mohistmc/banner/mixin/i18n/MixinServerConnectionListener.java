package com.mohistmc.banner.mixin.i18n;

import com.mohistmc.banner.BannerMCStart;
import net.minecraft.server.network.ServerConnectionListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerConnectionListener.class)
public class MixinServerConnectionListener {

    @ModifyConstant(method = "startTcpServerListener", constant = @Constant(stringValue = "Using default channel type"))
    private String banner$i18nChannel(String constant) {
        return BannerMCStart.I18N.get("networksystem.2");
    }

    @ModifyConstant(method = "startTcpServerListener", constant = @Constant(stringValue = "Using epoll channel type"))
    private String banner$i18nChannel1(String constant) {
        return BannerMCStart.I18N.get("networksystem.1");
    }
}
