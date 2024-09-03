package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(InteractWithDoor.class)
public class MixinInteractWithDoor {

    @Inject(method = "method_46966",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/DoorBlock;setOpen(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Z)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void banner$openDoor(BehaviorBuilder.Instance instance, MemoryAccessor memoryAccessor,
                                        MemoryAccessor memoryAccessor2, MutableObject mutableObject,
                                        MutableInt mutableInt, MemoryAccessor memoryAccessor3,
                                        ServerLevel serverLevel, LivingEntity livingEntity,
                                        long l, CallbackInfoReturnable<Boolean> cir, Path path,
                                        Optional optional, Node node, Node node2, BlockPos blockPos,
                                        BlockState blockState) {
        // CraftBukkit start - entities opening doors
        EntityInteractEvent event = new EntityInteractEvent(livingEntity.getBukkitEntity(), CraftBlock.at(livingEntity.level(), blockPos));
        livingEntity.level().getCraftServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
        // CraftBukkit end
    }
}
