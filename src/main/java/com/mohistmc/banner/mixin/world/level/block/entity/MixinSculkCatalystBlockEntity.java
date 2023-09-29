package com.mohistmc.banner.mixin.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SculkCatalystBlockEntity.class)
public abstract class MixinSculkCatalystBlockEntity extends BlockEntity {

    @Shadow @Final private SculkCatalystBlockEntity.CatalystListener catalystListener;

    public MixinSculkCatalystBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        this.catalystListener.banner$setLevel(level);
    }

    @Inject(method = "serverTick", at = @At("HEAD"))
    private static void banner$overrideSource(Level p_222780_, BlockPos p_222781_, BlockState p_222782_, SculkCatalystBlockEntity blockEntity, CallbackInfo ci) {
        CraftEventFactory.sourceBlockOverride = blockEntity.getBlockPos();
    }

    @Inject(method = "serverTick", at = @At("RETURN"))
    private static void banner$resetSource(Level p_222780_, BlockPos p_222781_, BlockState p_222782_, SculkCatalystBlockEntity blockEntity, CallbackInfo ci) {
        CraftEventFactory.sourceBlockOverride = null;
    }

    @Inject(method = "load", at = @At("HEAD"))
    private void banner$load(CompoundTag compoundTag, CallbackInfo ci) {
        super.load(compoundTag); // CraftBukkit - SPIGOT-7393: Load super Bukkit data
    }
}
