package com.mohistmc.banner.mixin.server.commands;

import net.minecraft.server.commands.TriggerCommand;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TriggerCommand.class)
public class MixinTriggerCommand {

    @Redirect(method = "getScore", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/scores/Scoreboard;getOrCreatePlayerScore(Lnet/minecraft/world/scores/ScoreHolder;Lnet/minecraft/world/scores/Objective;)Lnet/minecraft/world/scores/ScoreAccess;"))
    private static ScoreAccess banner$resetScore(Scoreboard instance, ScoreHolder scoreHolder, Objective objective) {
        return instance.getOrCreatePlayerScore(scoreHolder, objective);// CraftBukkit - SPIGOT-6917: use main scoreboard
    }
}
