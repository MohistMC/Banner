package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DropperBlock.class)
public class MixinDropperBlock {

    @Shadow @Final private static DispenseItemBehavior DISPENSE_BEHAVIOUR;

    @Shadow @Final private static Logger LOGGER;

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void dispenseFrom(ServerLevel worldIn, BlockState state, BlockPos pos) {
        DispenserBlockEntity dispensertileentity = worldIn.getBlockEntity(pos, BlockEntityType.DROPPER).orElse(null);

        if (dispensertileentity == null) {
            LOGGER.warn("Ignoring dispensing attempt for Dropper without matching block entity at {}", pos);
        } else {
            BlockSource blocksource = new BlockSource(worldIn, pos, state, dispensertileentity);

            int i = dispensertileentity.getRandomSlot(worldIn.getRandom());
            if (i < 0) {
                worldIn.levelEvent(1001, pos, 0);
            } else {
                ItemStack itemstack = dispensertileentity.getItem(i);
                if (!itemstack.isEmpty()) {
                    Direction direction = worldIn.getBlockState(pos).getValue(DispenserBlock.FACING);
                    Container iinventory = HopperBlockEntity.getContainerAt(worldIn, pos.relative(direction));
                    ItemStack itemstack1;
                    if (iinventory == null) {
                        itemstack1 = DISPENSE_BEHAVIOUR.dispense(blocksource, itemstack);
                    } else {
                        ItemStack split = itemstack.copy().split(1);
                        CraftItemStack craftItemStack = CraftItemStack.asCraftMirror(split);
                        Inventory destinationInventory;
                        // Have to special case large chests as they work oddly
                        if (iinventory instanceof CompoundContainer) {
                            destinationInventory = new CraftInventoryDoubleChest((CompoundContainer) iinventory);
                        } else {
                            destinationInventory =  iinventory.getOwner().getInventory();
                        }
                        InventoryMoveItemEvent event = new InventoryMoveItemEvent((dispensertileentity).getOwner().getInventory(), craftItemStack, destinationInventory, true);
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            return;
                        }
                        itemstack1 = HopperBlockEntity.addItem(dispensertileentity, iinventory, CraftItemStack.asNMSCopy(event.getItem()), direction.getOpposite());
                        if (event.getItem().equals(craftItemStack) && itemstack1.isEmpty()) {
                            itemstack1 = itemstack.copy();
                            itemstack1.shrink(1);
                        } else {
                            itemstack1 = itemstack.copy();
                        }
                    }

                    dispensertileentity.setItem(i, itemstack1);
                }
            }
        }
    }
}
