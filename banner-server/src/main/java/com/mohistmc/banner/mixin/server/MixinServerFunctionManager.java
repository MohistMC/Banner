package com.mohistmc.banner.mixin.server;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerFunctionManager.class)
public class MixinServerFunctionManager {

    @Shadow @Final MinecraftServer server;

    @Inject(method = "getDispatcher", cancellable = true, at = @At("HEAD"))
    private void banner$useVanillaDispatcher(CallbackInfoReturnable<CommandDispatcher<CommandSourceStack>> cir) {
        cir.setReturnValue(this.server.bridge$getVanillaCommands().getDispatcher());
    }
}
