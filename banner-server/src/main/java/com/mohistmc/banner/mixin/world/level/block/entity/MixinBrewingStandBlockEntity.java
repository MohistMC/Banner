package com.mohistmc.banner.mixin.world.level.block.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.block.BrewingStartEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.InventoryHolder;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = BrewingStandBlockEntity.class, priority = 300)
public abstract class MixinBrewingStandBlockEntity extends BaseContainerBlockEntity {

    @Shadow private NonNullList<ItemStack> items;
    public List<HumanEntity> transaction = new ArrayList<>();
    private int maxStack = MAX_STACK;

    protected MixinBrewingStandBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(at = @At("HEAD"), method = "serverTick", cancellable = true)
    private static void doBukkitEvent_BrewingStandFuelEvent(Level level, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
        ItemStack itemstack = (ItemStack) (blockEntity).getContents().get(4);

        if (blockEntity.fuel <= 0 && itemstack.getItem() == Items.BLAZE_POWDER) {
            BrewingStandFuelEvent event = new BrewingStandFuelEvent(blockEntity.getLevel().getWorld().getBlockAt(blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ()), CraftItemStack.asCraftMirror(itemstack), 20);
            level.getCraftServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                ci.cancel();
                return;
            }

            blockEntity.fuel = event.getFuelPower();
            if (blockEntity.fuel > 0 && event.isConsuming()) itemstack.shrink(1);
        }
    }

    @Inject(method = "serverTick",
            at = @At (value = "CONSTANT",
                    args = "intValue=20"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private static void banner$brewEvent(Level level, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity, CallbackInfo ci, ItemStack itemStack) {
        BrewingStandFuelEvent event = new BrewingStandFuelEvent(CraftBlock.at(level, pos), CraftItemStack.asCraftMirror(itemStack), 20);
        level.getCraftServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
            return;
        }

        blockEntity.fuel = event.getFuelPower();
        if (blockEntity.fuel > 0 && event.isConsuming()) {
            itemStack.shrink(1);
        }
    }

    @Redirect(method = "serverTick",
            at = @At (value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private static void banner$isConsuming(ItemStack stack, int amount) {}

    @Inject(method = "serverTick", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;ingredient:Lnet/minecraft/world/item/Item;"))
    private static void banner$brewBegin(Level level, BlockPos pos, BlockState p_155288_, BrewingStandBlockEntity entity, CallbackInfo ci) {
        var event = new BrewingStartEvent(CraftBlock.at(level, pos), CraftItemStack.asCraftMirror(entity.getItem(3)), entity.brewTime);
        Bukkit.getPluginManager().callEvent(event);
        entity.brewTime = event.getTotalBrewTime();
    }

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
    public int getMaxStackSize() {
        if (maxStack == 0) maxStack = MAX_STACK;
        return maxStack;
    }

    @Override
    public void setMaxStackSize(int size) {
        this.maxStack = size;
    }

}
