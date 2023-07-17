package com.mohistmc.banner.mixin.server.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.event.world.TimeSkipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(TimeCommand.class)
public class MixinTimeCommand {

    @Redirect(method = "setTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    private static Iterable<ServerLevel> banner$useSourceLevel1(MinecraftServer server, CommandSourceStack source) {
        return List.of(source.getLevel());
    }

    @Redirect(method = "addTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    private static Iterable<ServerLevel> banner$useSourceLevel2(MinecraftServer server, CommandSourceStack source) {
        return List.of(source.getLevel());
    }

    @Redirect(method = "addTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V"))
    private static void banner$addTimeEvent(ServerLevel serverWorld, long time) {
        TimeSkipEvent event = new TimeSkipEvent(serverWorld.getWorld(), TimeSkipEvent.SkipReason.COMMAND, time - serverWorld.getDayTime());
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            serverWorld.setDayTime(serverWorld.getDayTime() + event.getSkipAmount());
        }
    }

    @Redirect(method = "setTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V"))
    private static void banner$setTimeEvent(ServerLevel serverWorld, long time) {
        TimeSkipEvent event = new TimeSkipEvent(serverWorld.getWorld(), TimeSkipEvent.SkipReason.COMMAND, time - serverWorld.getDayTime());
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            serverWorld.setDayTime(serverWorld.getDayTime() + event.getSkipAmount());
        }
    }
}
