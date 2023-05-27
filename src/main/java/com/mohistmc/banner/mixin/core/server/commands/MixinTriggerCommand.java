package com.mohistmc.banner.mixin.core.server.commands;

import net.minecraft.server.commands.TriggerCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TriggerCommand.class)
public class MixinTriggerCommand {

    @Redirect(method = "getScore", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;getScoreboard()Lnet/minecraft/world/scores/Scoreboard;"))
    private static Scoreboard banner$resetScore(ServerPlayer instance) {
        return instance.getServer().getScoreboard();// CraftBukkit - SPIGOT-6917: use main scoreboard
    }
}
