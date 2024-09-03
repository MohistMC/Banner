package com.mohistmc.banner.mixin.server.commands;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ScheduleCommand;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.timers.TimerQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScheduleCommand.class)
public class MixinScheduleCommand {

    private static final AtomicReference<CommandSourceStack> banner$source = new AtomicReference<>();

    @Inject(method = "schedule", at = @At("HEAD"))
    private static void banner$getSource(CommandSourceStack commandSourceStack, Pair<ResourceLocation, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> pair, int i, boolean bl, CallbackInfoReturnable<Integer> cir) {
        banner$source.set(commandSourceStack);
    }

    @Redirect(method = "schedule",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/ServerLevelData;getScheduledEvents()Lnet/minecraft/world/level/timers/TimerQueue;"))
    private static TimerQueue<MinecraftServer> banner$resetTimer(ServerLevelData instance) {
        return banner$source.get().getLevel().bridge$serverLevelDataCB().overworldData().getScheduledEvents(); // CraftBukkit - SPIGOT-6667: Use world specific function timer;
    }
}
