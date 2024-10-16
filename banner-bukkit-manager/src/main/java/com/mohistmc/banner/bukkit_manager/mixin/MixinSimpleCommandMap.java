package com.mohistmc.banner.bukkit_manager.mixin;

import com.mohistmc.banner.command.DumpCommand;
import com.mohistmc.banner.command.GetPluginListCommand;
import com.mohistmc.banner.command.ModListCommand;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SimpleCommandMap.class,remap = false)
public abstract class MixinSimpleCommandMap {

    @Shadow public abstract boolean register(@NotNull String fallbackPrefix, @NotNull Command command);

    @Inject(method = "setDefaultCommands()V", at = @At("RETURN"))
    private void banner$newCommand(CallbackInfo cir) {
        register("banner", new ModListCommand("mods"));
        register("banner", new DumpCommand("dump"));
        register("banner", new GetPluginListCommand("pluginlist"));
    }
}
