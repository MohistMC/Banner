package com.mohistmc.banner.mixin.world.level.block.entity;

import com.mohistmc.banner.bukkit.DistValidate;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChiseledBookShelfBlockEntity.class)
public abstract class MixinChiseledBookShelfBlockEntity extends BlockEntity implements Container {

    @Shadow @Final private NonNullList<ItemStack> items;

    @Shadow protected abstract void updateState(int i);

    public MixinChiseledBookShelfBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public List<HumanEntity> transaction = new ArrayList<>();
    private int maxStack = 1;

    @Override
    public List<ItemStack> getContents() {
        return this.items;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public void setOwner(InventoryHolder owner) {
    }

    @Override
    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public Location getLocation() {
        if (!DistValidate.isValid(level)) return null;
        return new org.bukkit.Location(level.getWorld(), worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
    }

    @Redirect(method = "setItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/ChiseledBookShelfBlockEntity;updateState(I)V"))
    private void fixShapeUpdate(ChiseledBookShelfBlockEntity instance, int i) {
        if (this.level != null) {
            this.updateState(i); // CraftBukkit - SPIGOT-7381: check for null world
        }
    }

    @Inject(method = "loadAdditional", at = @At("HEAD"))
    private void banner$load(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        super.loadAdditional(compoundTag, provider); // CraftBukkit - SPIGOT-7393: Load super Bukkit data
    }

    @Inject(method = "removeItem",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/ChiseledBookShelfBlockEntity;updateState(I)V"))
    private void banner$checkWorld(int i, int j, CallbackInfoReturnable<ItemStack> cir) {
        if (level == null) {
            cir.cancel(); // CraftBukkit - SPIGOT-7381: check for null world
        }
    }

    @Inject(method = "setItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/ChiseledBookShelfBlockEntity;updateState(I)V"),
            cancellable = true)
    private void banner$checkWorld0(int i, ItemStack itemStack, CallbackInfo ci) {
        if (level == null) {
            ci.cancel(); // CraftBukkit - SPIGOT-7381: check for null world
        }
    }
}
