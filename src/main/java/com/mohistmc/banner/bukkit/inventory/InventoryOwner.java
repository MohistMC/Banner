package com.mohistmc.banner.bukkit.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * @author Mgazul
 * @date 2020/4/10 13:39
 */
public class InventoryOwner {

    public static Inventory getInventory(Container inventory) {
        InventoryHolder owner = get(inventory);
        return (owner == null ? new CraftCustomInventory(inventory).getInventory() : owner.getInventory());
    }

    public static InventoryHolder get(BlockEntity te) {
        return get(te.getLevel(), te.getBlockPos(), true);
    }

    public static InventoryHolder get(Container inventory) {
        try {
            return inventory.getOwner();
        } catch (AbstractMethodError e) {
            return (inventory instanceof BlockEntity blockEntity) ? get(blockEntity) : null;
        }
    }

    public static InventoryHolder get(Level world, BlockPos pos, boolean useSnapshot) {
        if (world == null) return null;
        // Spigot start
        org.bukkit.block.Block block = world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        if (block == null) {
            return null;
        }
        // Spigot end
        org.bukkit.block.BlockState state = block.getState();
        if (state instanceof InventoryHolder) {
            return (InventoryHolder) state;
        } else if (state instanceof CraftBlockEntityState<? extends BlockEntity> blockEntityState) {
            BlockEntity te = blockEntityState.getTileEntity();
            if (te instanceof Container container) {
                return new CraftCustomInventory(container);
            }
        }
        return null;
    }
}

