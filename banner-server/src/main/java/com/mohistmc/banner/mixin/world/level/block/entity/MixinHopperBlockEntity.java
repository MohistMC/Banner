package com.mohistmc.banner.mixin.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

// Banner TODO fix patches
@Mixin(HopperBlockEntity.class)
public abstract class MixinHopperBlockEntity extends RandomizableContainerBlockEntity implements Hopper {

    // @formatter:off
    @Shadow private NonNullList<ItemStack> items;
    @Shadow public abstract void setItem(int index, ItemStack stack);
    @Shadow private static boolean tryMoveItems(Level p_155579_, BlockPos p_155580_, BlockState p_155581_, HopperBlockEntity p_155582_, BooleanSupplier p_155583_) { return false; }
    // @formatter:on

    @Shadow protected abstract boolean inventoryFull();

    public List<HumanEntity> transaction = new ArrayList<>();
    private int maxStack = MAX_STACK;

    protected MixinHopperBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    /*
    @Redirect(method = "pushItemsTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;tryMoveItems(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/HopperBlockEntity;Ljava/util/function/BooleanSupplier;)Z"))
    private static boolean banner$hopperCheck(Level level, BlockPos pos, BlockState state, HopperBlockEntity hopper, BooleanSupplier flag) {
        var result = tryMoveItems(level, pos, state, hopper, flag);
        if (!result && DistValidate.isValid(level) && level.bridge$spigotConfig().hopperCheck > 1) {
            hopper.setCooldown(level.bridge$spigotConfig().hopperCheck);
        }
        return result;
    }

    private static AtomicReference<HopperBlockEntity> banner$hopperEntity = new AtomicReference<>();

    @Inject(method = "tryMoveItems", at = @At("HEAD"))
    private static void banner$setHopper(Level level, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, BooleanSupplier validator, CallbackInfoReturnable<Boolean> cir) {
        banner$hopperEntity.set(blockEntity);
    }

    /*
    @Redirect(method = "tryMoveItems",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;ejectItems(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/Container;)Z"))
    private static boolean banner$changeEjects(Level level, BlockPos pos, BlockState state, Container sourceContainer) {
        return ejectItems(level, pos, state, sourceContainer, banner$hopperEntity.get());
    }*/

    /*
    private static AtomicReference<HopperBlockEntity> banner$hopper = new AtomicReference<>();
    private static AtomicReference<Level> banner$world = new AtomicReference<>();
    private static AtomicReference<InventoryMoveItemEvent> banner$moveEvent = new AtomicReference<>();

    /*
    private static boolean ejectItems(Level world, BlockPos blockposition, BlockState iblockdata, Container iinventory, HopperBlockEntity hopper) { // CraftBukkit
        banner$hopper.set(hopper);
        banner$world.set(world);
        return shadow$ejectItems(world, blockposition, iblockdata, iinventory);
    }*/

    /*
    @Redirect(method = "ejectItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack banner$moveItem(Container source, Container destination, ItemStack stack, Direction direction) {
        var entity = banner$hopper.getAndSet(null);
        var level = banner$world.getAndSet(null);
        entity = entity == null ? (HopperBlockEntity) source : entity;
        CraftItemStack original = CraftItemStack.asCraftMirror(stack);

        Inventory destinationInventory;
        // Have to special case large chests as they work oddly
        if (destination instanceof CompoundContainer) {
            destinationInventory = new CraftInventoryDoubleChest(((CompoundContainer) destination));
        } else {
            destinationInventory = destination.getOwner() == null ? null: destination.getOwner().getInventory();
        }

        ItemStack stack1 = stack;
        if (destinationInventory != null) {
            InventoryMoveItemEvent event = new InventoryMoveItemEvent(InventoryOwner.getInventory(entity), original.clone(), destinationInventory, true);
            Bukkit.getPluginManager().callEvent(event);
            banner$moveEvent.set(event);
            if (event.isCancelled()) {
                entity.setCooldown(level.bridge$spigotConfig().hopperTransfer); // Delay hopper checks
                return null;
            }
            stack1 = CraftItemStack.asNMSCopy(event.getItem());
        }

        return HopperBlockEntity.addItem(source, destination, stack1, direction);
    }

    @Inject(method = "ejectItems",
            at = @At(value = "INVOKE",
            shift = At.Shift.BY,
            target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"),
            cancellable = true)
    private static void banner$cancelIfNotEject(Level level, BlockPos pos, BlockState state, Container sourceContainer, CallbackInfoReturnable<Boolean> cir) {
        if (banner$moveEvent.get() != null && banner$moveEvent.getAndSet(null).isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "tryTakeInItemFromSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack banner$pullItem(Container source, Container destination, ItemStack stack, Direction direction, Hopper hopper, Container inv, int index) {
        ItemStack origin = inv.getItem(index).copy();
        CraftItemStack original = CraftItemStack.asCraftMirror(stack);

        Inventory sourceInventory;
        // Have to special case large chests as they work oddly
        if (source instanceof CompoundContainer) {
            sourceInventory = new CraftInventoryDoubleChest(((CompoundContainer) source));
        } else {
            sourceInventory = source.getOwner() == null ? null : source.getOwner().getInventory();
        }

        if (sourceInventory != null) {
            InventoryMoveItemEvent event = new InventoryMoveItemEvent(sourceInventory, original.clone(), destination.getOwner().getInventory(), false);
            Bukkit.getPluginManager().callEvent(event);
            banner$moveEvent.set(event);
            if (event.isCancelled()) {
                inv.setItem(index, origin);
                if (destination instanceof HopperBlockEntity) {
                    ((HopperBlockEntity) destination).setCooldown(8); // Delay hopper checks
                }
                return null;
            }
            return HopperBlockEntity.addItem(source, destination, CraftItemStack.asNMSCopy(event.getItem()), direction);
        }

        return HopperBlockEntity.addItem(source, destination, stack, direction);
    }

    @Inject(method = "tryTakeInItemFromSlot",
            at = @At(value = "INVOKE",
            shift = At.Shift.BY,
            target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
                    ordinal = 1),
            cancellable = true)
    private static void banner$cancelIfNotTaken(Hopper hopper, Container container, int slot, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (banner$moveEvent.get() != null && banner$moveEvent.getAndSet(null).isCancelled()) {
            cir.setReturnValue(false);
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
    }*/

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
