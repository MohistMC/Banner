package com.mohistmc.banner.mixin.server.commands;

import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.DifficultyCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DifficultyCommand.class)
public class MixinDifficultyCommand {

    private static final AtomicReference<ServerLevel> banner$serverLevel = new AtomicReference<>();

    @Inject(method = "setDifficulty", at = @At("HEAD"))
    private static void banner$getServerLevel(CommandSourceStack source, Difficulty difficulty, CallbackInfoReturnable<Integer> cir) {
        banner$serverLevel.set(source.getLevel());
    }

    @Redirect(method = "setDifficulty",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/WorldData;getDifficulty()Lnet/minecraft/world/Difficulty;"))
    private static Difficulty banner$getDifficult(WorldData instance) {
        return banner$serverLevel.getAndSet(null).getDifficulty();
    }

    @Redirect(method = "setDifficulty",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;setDifficulty(Lnet/minecraft/world/Difficulty;Z)V"))
    private static void banner$resetDifficulty(MinecraftServer instance, Difficulty difficulty, boolean forced) {
        banner$serverLevel.getAndSet(null).bridge$serverLevelDataCB().setDifficulty(difficulty);
    }
}
