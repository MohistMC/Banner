package com.mohistmc.banner.mixin.network.connection;

import com.mohistmc.banner.injection.network.connection.InjectionConnection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Connection.class)
public class MixinConnection implements InjectionConnection {

    @Shadow public Channel channel;
    public String hostname = ""; // CraftBukkit - add field

    @Redirect(method = "disconnect", at = @At(value = "INVOKE",
            target = "Lio/netty/channel/Channel;close()Lio/netty/channel/ChannelFuture;"))
    private ChannelFuture banner$disconnect(Channel instance) {
        return this.channel.close();// We can't wait as this may be called from an event loop.
    }

    @Override
    public String bridge$hostname() {
        return hostname;
    }

    @Override
    public void banner$setHostName(String hostName) {
        hostname = hostName;
    }
}
