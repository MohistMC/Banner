package com.mohistmc.banner.mixin.core.server.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.commands.PlaceCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlaceCommand.class)
public class MixinPlaceCommand {

    @Inject(method = "placeStructure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/StructureStart;getBoundingBox()Lnet/minecraft/world/level/levelgen/structure/BoundingBox;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void banner$pushStructureReason(CommandSourceStack commandSourceStack, Holder.Reference<Structure> reference, BlockPos blockPos, CallbackInfoReturnable<Integer> cir, ServerLevel serverLevel, Structure structure, ChunkGenerator chunkGenerator, StructureStart structureStart) {
        structureStart.banner$setGenerationEventCause(org.bukkit.event.world.AsyncStructureGenerateEvent.Cause.COMMAND); // CraftBukkit - set AsyncStructureGenerateEvent.Cause.COMMAND as generation cause
    }
}
