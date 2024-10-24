package com.mohistmc.banner.mixin.server.commands;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.commands.PlaceCommand;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlaceCommand.class)
public class MixinPlaceCommand {

    @Inject(method = "placeStructure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/StructureStart;getBoundingBox()Lnet/minecraft/world/level/levelgen/structure/BoundingBox;"))
    private static void banner$pushStructureReason(CommandSourceStack commandSourceStack, Holder.Reference<Structure> reference, BlockPos blockPos, CallbackInfoReturnable<Integer> cir, @Local StructureStart structureStart) {
        structureStart.banner$setGenerationEventCause(org.bukkit.event.world.AsyncStructureGenerateEvent.Cause.COMMAND); // CraftBukkit - set AsyncStructureGenerateEvent.Cause.COMMAND as generation cause
    }
}
