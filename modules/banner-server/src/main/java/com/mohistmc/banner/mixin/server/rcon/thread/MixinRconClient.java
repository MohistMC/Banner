package com.mohistmc.banner.mixin.server.rcon.thread;

import java.net.Socket;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.rcon.RconConsoleSource;
import net.minecraft.server.rcon.thread.RconClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Mgazul by MohistMC
 * @date 2023/9/29 17:40:45
 */
@Mixin(RconClient.class)
public class MixinRconClient {

    private DedicatedServer banner$serverInterface;

    // CraftBukkit start
    private RconConsoleSource rconConsoleSource;
    // CraftBukkit end


    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void banner$init(ServerInterface pServerInterface, String pRconPassword, Socket pClient, CallbackInfo ci) {
        banner$serverInterface = (DedicatedServer) pServerInterface;
        this.rconConsoleSource = new net.minecraft.server.rcon.RconConsoleSource(banner$serverInterface); // CraftBukkit
        this.rconConsoleSource.banner$setSocketAddress(pClient.getRemoteSocketAddress()); // CraftBukkit
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/rcon/thread/RconClient;sendCmdResponse(ILjava/lang/String;)V", ordinal = 0))
    private void banner$checkHeart(CallbackInfo ci) {
        banner$serverInterface.banner$setRconConsoleSource(rconConsoleSource);
    }
}
