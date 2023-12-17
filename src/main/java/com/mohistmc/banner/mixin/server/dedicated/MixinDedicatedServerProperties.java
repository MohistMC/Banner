package com.mohistmc.banner.mixin.server.dedicated;

import java.util.Properties;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DedicatedServerProperties.class)
public class MixinDedicatedServerProperties {

    @Mutable
    @Shadow @Final
    public long maxTickTime;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$resetMaxTickTime(Properties properties, CallbackInfo ci) {
        this.maxTickTime = -1;
    }
}
