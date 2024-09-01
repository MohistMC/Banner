package com.mohistmc.banner.mixin.server.network;

import com.mohistmc.banner.injection.server.network.InjectionServerConnectionListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import java.util.List;
import net.minecraft.server.network.ServerConnectionListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerConnectionListener.class)
public class MixinServerConnectionListener implements InjectionServerConnectionListener {

    @Shadow @Final private List<ChannelFuture> channels;

    @Redirect(method = "startTcpServerListener", at = @At(value = "INVOKE",
            target = "Lio/netty/bootstrap/ServerBootstrap;bind()Lio/netty/channel/ChannelFuture;",
            remap = false))
    public ChannelFuture banner$bind(ServerBootstrap bootstrap) {
        return bootstrap.option(ChannelOption.AUTO_READ, false).bind();
    }

    // CraftBukkit start
    @Override
    public void acceptConnections() {
        synchronized (this.channels) {
            for (ChannelFuture future : this.channels) {
                future.channel().config().setAutoRead(true);
            }
        }
    }
    // CraftBukkit end
}
