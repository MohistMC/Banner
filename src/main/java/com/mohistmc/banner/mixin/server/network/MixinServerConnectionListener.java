package com.mohistmc.banner.mixin.server.network;

import com.mohistmc.banner.injection.server.network.InjectionServerConnectionListener;
import io.netty.channel.ChannelFuture;
import net.minecraft.server.network.ServerConnectionListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ServerConnectionListener.class)
public class MixinServerConnectionListener implements InjectionServerConnectionListener {

    @Shadow @Final private List<ChannelFuture> channels;

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
