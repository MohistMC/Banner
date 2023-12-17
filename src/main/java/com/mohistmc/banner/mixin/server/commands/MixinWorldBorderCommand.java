package com.mohistmc.banner.mixin.server.commands;

import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.WorldBorderCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldBorderCommand.class)
public class MixinWorldBorderCommand {

    private static AtomicReference<CommandSourceStack> banner$source = new AtomicReference<>();

    @Inject(method = "setDamageBuffer", at = @At("HEAD"))
    private static void banner$setSource(CommandSourceStack source, float distance, CallbackInfoReturnable<Integer> cir) {
        banner$source.set(source);
    }

    @Redirect(method = "setDamageBuffer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    private static WorldBorder banner$resetBorder(ServerLevel instance) {
        return banner$source.get().getLevel().getWorldBorder();
    }

    @Inject(method = "setDamageAmount", at = @At("HEAD"))
    private static void banner$setSource0(CommandSourceStack source, float distance, CallbackInfoReturnable<Integer> cir) {
        banner$source.set(source);
    }

    @Redirect(method = "setDamageAmount", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    private static WorldBorder banner$resetBorder0(ServerLevel instance) {
        return banner$source.get().getLevel().getWorldBorder();
    }

    @Inject(method = "setWarningTime", at = @At("HEAD"))
    private static void banner$setSource1(CommandSourceStack source, int time, CallbackInfoReturnable<Integer> cir) {
        banner$source.set(source);
    }

    @Redirect(method = "setWarningTime", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    private static WorldBorder banner$resetBorder1(ServerLevel instance) {
        return banner$source.get().getLevel().getWorldBorder();
    }

    @Inject(method = "setWarningDistance", at = @At("HEAD"))
    private static void banner$setSource2(CommandSourceStack source, int time, CallbackInfoReturnable<Integer> cir) {
        banner$source.set(source);
    }

    @Redirect(method = "setWarningDistance", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    private static WorldBorder banner$resetBorder2(ServerLevel instance) {
        return banner$source.get().getLevel().getWorldBorder();
    }

    @Inject(method = "getSize", at = @At("HEAD"))
    private static void banner$setSource3(CommandSourceStack source, CallbackInfoReturnable<Integer> cir) {
        banner$source.set(source);
    }

    @Redirect(method = "getSize", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    private static WorldBorder banner$resetBorder3(ServerLevel instance) {
        return banner$source.get().getLevel().getWorldBorder();
    }

    @Inject(method = "setCenter", at = @At("HEAD"))
    private static void banner$setSource4(CommandSourceStack source, Vec2 pos, CallbackInfoReturnable<Integer> cir) {
        banner$source.set(source);
    }

    @Redirect(method = "setCenter", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    private static WorldBorder banner$resetBorder4(ServerLevel instance) {
        return banner$source.get().getLevel().getWorldBorder();
    }

    @Inject(method = "setSize", at = @At("HEAD"))
    private static void banner$setSource5(CommandSourceStack source, double newSize, long time, CallbackInfoReturnable<Integer> cir) {
        banner$source.set(source);
    }

    @Redirect(method = "setSize", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    private static WorldBorder banner$resetBorder5(ServerLevel instance) {
        return banner$source.get().getLevel().getWorldBorder();
    }
}
