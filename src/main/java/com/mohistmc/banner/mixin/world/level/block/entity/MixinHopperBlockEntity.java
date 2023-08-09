package com.mohistmc.banner.mixin.world.level.block.entity;

import com.mohistmc.banner.bukkit.DistValidate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.HopperInventorySearchEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(HopperBlockEntity.class)
public abstract class MixinHopperBlockEntity extends RandomizableContainerBlockEntity implements Hopper {

    @Unique
    public List<HumanEntity> transaction = new ArrayList<>();
    // @formatter:off
    @Shadow private NonNullList<ItemStack> items;

    // @formatter:on
    @Unique
    private int maxStack = MAX_STACK;
    protected MixinHopperBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Shadow private static boolean tryMoveItems(Level p_155579_, BlockPos p_155580_, BlockState p_155581_, HopperBlockEntity p_155582_, BooleanSupplier p_155583_) { return false; }

    @Redirect(method = "pushItemsTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;tryMoveItems(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/HopperBlockEntity;Ljava/util/function/BooleanSupplier;)Z"))
    private static boolean banner$hopperCheck(Level level, BlockPos pos, BlockState state, HopperBlockEntity hopper, BooleanSupplier flag) {
        var result = tryMoveItems(level, pos, state, hopper, flag);
        if (!result && DistValidate.isValid(level) && level.bridge$spigotConfig().hopperCheck > 1) {
            hopper.setCooldown(level.bridge$spigotConfig().hopperCheck);
        }
        return result;
    }

    @Inject(method = "ejectItems", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;removeItem(II)Lnet/minecraft/world/item/ItemStack;"))
    private static void banner$ejectItems(Level level, BlockPos pos, BlockState state, Container sourceContainer, CallbackInfoReturnable<Boolean> cir, Container container, Direction direction, int i, ItemStack itemStack) {
        Container source = sourceContainer;
        Container destination = container;
        HopperBlockEntity entity = (HopperBlockEntity) source;
        CraftItemStack original = CraftItemStack.asCraftMirror(itemStack);

        Inventory destinationInventory;
        // Have to special case large chests as they work oddly
        if (destination instanceof CompoundContainer) {
            destinationInventory = new CraftInventoryDoubleChest(((CompoundContainer) destination));
        } else {
            destinationInventory = destination.getOwner().getInventory();
        }

        InventoryMoveItemEvent event = new InventoryMoveItemEvent(source.getOwner().getInventory(), original.clone(), destinationInventory, true);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            entity.setCooldown(level.bridge$spigotConfig().hopperTransfer);
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "tryTakeInItemFromSlot", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;removeItem(II)Lnet/minecraft/world/item/ItemStack;"))
    private static void banner$tryTakeInItemFromSlot(Hopper hopper, Container container, int slot, Direction direction, CallbackInfoReturnable<Boolean> cir, ItemStack itemStack, ItemStack itemStack2) {
        Container source = container;
        Container destination = hopper;
        CraftItemStack original = CraftItemStack.asCraftMirror(itemStack);

        Inventory sourceInventory;
        // Have to special case large chests as they work oddly
        if (source instanceof CompoundContainer) {
            sourceInventory = new CraftInventoryDoubleChest(((CompoundContainer) source));
        } else {
            sourceInventory = source.getOwner().getInventory();
        }

        InventoryMoveItemEvent event = new InventoryMoveItemEvent(sourceInventory, original.clone(), destination.getOwner().getInventory(), false);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            if (destination instanceof HopperBlockEntity) {
                ((HopperBlockEntity) destination).setCooldown(8);
            }
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/item/ItemEntity;)Z", cancellable = true, at = @At("HEAD"))
    private static void banner$pickupItem(Container inventory, ItemEntity itemEntity, CallbackInfoReturnable<Boolean> cir) {
        InventoryPickupItemEvent event = new InventoryPickupItemEvent(inventory.getOwner().getInventory(), (Item) itemEntity.getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static Container runHopperInventorySearchEvent(Container inventory, CraftBlock hopper, CraftBlock searchLocation, HopperInventorySearchEvent.ContainerType containerType) {
        var event = new HopperInventorySearchEvent((inventory != null) ? new CraftInventory(inventory) : null, containerType, hopper, searchLocation);
        Bukkit.getServer().getPluginManager().callEvent(event);
        CraftInventory craftInventory = (CraftInventory) event.getInventory();
        return (craftInventory != null) ? craftInventory.getInventory() : null;
    }

    @Inject(method = "getAttachedContainer", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At("RETURN"))
    private static void banner$searchTo(Level level, BlockPos pos, BlockState p_155595_, CallbackInfoReturnable<Container> cir, Direction direction) {
        var container = cir.getReturnValue();
        var hopper = CraftBlock.at(level, pos);
        var searchBlock = CraftBlock.at(level, pos.relative(direction));
        cir.setReturnValue(runHopperInventorySearchEvent(container, hopper, searchBlock, HopperInventorySearchEvent.ContainerType.DESTINATION));
    }

    @Inject(method = "getSourceContainer", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At("RETURN"))
    private static void banner$searchFrom(Level level, Hopper hopper, CallbackInfoReturnable<Container> cir) {
        var container = cir.getReturnValue();
        var blockPos = BlockPos.containing(hopper.getLevelX(), hopper.getLevelY(), hopper.getLevelZ());
        var hopperBlock = CraftBlock.at(level, blockPos);
        var containerBlock = CraftBlock.at(level, blockPos.above());
        cir.setReturnValue(runHopperInventorySearchEvent(container, hopperBlock, containerBlock, HopperInventorySearchEvent.ContainerType.SOURCE));
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
