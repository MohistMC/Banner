package com.mohistmc.banner.mixin.server.commands;

import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.SetSpawnCommand;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SetSpawnCommand.class)
public class MixinSetSpawnCommand {

    @Inject(method = "setSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;FZZ)V"))
    private static void banner$addCause(CommandSourceStack source, Collection<ServerPlayer> targets, BlockPos pos, float angle, CallbackInfoReturnable<Integer> cir) {
        source.getLevel().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.COMMAND);
    }
}
