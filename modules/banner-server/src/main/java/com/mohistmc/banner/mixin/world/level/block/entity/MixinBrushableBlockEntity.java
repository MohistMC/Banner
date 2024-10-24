package com.mohistmc.banner.mixin.world.level.block.entity;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BrushableBlockEntity.class)
public abstract class MixinBrushableBlockEntity extends BlockEntity {

    public MixinBrushableBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Redirect(method = "dropContent", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$dropContent(Level instance, Entity entity) {
        return false;
    }

    @Inject(method = "dropContent", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$dropEvent(Player player, CallbackInfo ci, double d, double e, double f,
                                  Direction direction, BlockPos blockPos, double g, double h,
                                  double i, ItemEntity itemEntity) {
        // CraftBukkit start
        org.bukkit.block.Block bblock = CraftBlock.at(this.level, this.worldPosition);
        CraftEventFactory.handleBlockDropItemEvent(bblock, bblock.getState(), (ServerPlayer) player, Arrays.asList(itemEntity));
        // CraftBukkit end
    }

    @Inject(method = "loadAdditional", at = @At("HEAD"))
    private void banner$load(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        super.loadAdditional(compoundTag, provider); // CraftBukkit - SPIGOT-7393: Load super Bukkit data
    }
}
