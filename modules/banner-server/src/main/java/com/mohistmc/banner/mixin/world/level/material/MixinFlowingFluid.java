package com.mohistmc.banner.mixin.world.level.material;

import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.banner.bukkit.DistValidate;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FlowingFluid.class)
public abstract class MixinFlowingFluid {

    @Inject(method = "spread", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FlowingFluid;spreadTo(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)V"))
    public void banner$flowInto(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        if (!DistValidate.isValid(serverLevel)) return;
        Block source = CraftBlock.at(serverLevel, blockPos);
        BlockFromToEvent event = new BlockFromToEvent(source, BlockFace.DOWN);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "spreadToSides", cancellable = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/material/FlowingFluid;spreadTo(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)V"))
    public void banner$flowInto(ServerLevel serverLevel, BlockPos blockPos, FluidState fluidState, BlockState blockState, CallbackInfo ci, @Local Direction direction) {
        if (!DistValidate.isValid(serverLevel)) return;
        Block source = CraftBlock.at(serverLevel, blockPos);
        BlockFromToEvent event = new BlockFromToEvent(source, CraftBlock.notchToBlockFace(direction));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Decorate(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean banner$fluidLevelChange(ServerLevel instance, BlockPos blockPos, BlockState blockState, int i) throws Throwable {
        if (DistValidate.isValid(instance)) {
            FluidLevelChangeEvent event = CraftEventFactory.callFluidLevelChangeEvent(instance, blockPos, blockState);
            if (event.isCancelled()) {
                return (boolean) DecorationOps.cancel().invoke();
            } else {
                blockState = ((CraftBlockData) event.getNewData()).getState();
            }
        }
        return (boolean) DecorationOps.callsite().invoke(instance, blockPos, blockState, i);
    }
}
