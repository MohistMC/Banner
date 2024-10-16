package com.mohistmc.banner.mixin.core.dispenser;

import com.mohistmc.banner.bukkit.BukkitFieldHooks;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftVector;
import org.bukkit.event.block.BlockDispenseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DefaultDispenseItemBehavior.class)
public class MixinDefaultDispenseItemBehavior {

    // CraftBukkit start
    @Unique
    private boolean dropper;
    @Unique
    private static transient BlockSource banner$isourceblock;
    @Unique
    private static transient boolean banner$dropper;
    @Unique
    private static transient ItemEntity banner$itemEntity;

    @Unique
    public void banner$constructor() {
        throw new RuntimeException();
    }

    @Unique
    public void banner$constructor(boolean dropper) {
        banner$constructor();
        this.dropper = dropper;
    }

    /**
     * @author wdog5
     * @reason
     */
    /*
    @Overwrite
    public static void spawnItem(Level level, ItemStack stack, int speed, Direction facing, Position position) {
       position = DispenserBlock.getDispensePosition(banner$isourceblock);
        double d = position.x();
        double e = position.y();
        double f = position.z();
        if (facing.getAxis() == Direction.Axis.Y) {
            e -= 0.125;
        } else {
            e -= 0.15625;
        }

        ItemEntity itemEntity = new ItemEntity(level, d, e, f, stack);
        banner$itemEntity = itemEntity;
        double g = level.random.nextDouble() * 0.1 + 0.2;
        itemEntity.setDeltaMovement(level.random.triangle((double)facing.getStepX() * g, 0.0172275 * (double)speed), level.random.triangle(0.2, 0.0172275 * (double)speed), level.random.triangle((double)facing.getStepZ() * g, 0.0172275 * (double)speed));
        //spawnItem(level, stack, speed, facing, banner$isourceblock, banner$dropper);
        level.addFreshEntity(itemEntity);
    }*/

    @Unique
    private static boolean spawnItem(Level level, ItemStack stack, int speed, Direction facing, BlockSource isourceblock, boolean dropper) {
        banner$dropper = dropper;
        banner$isourceblock = isourceblock;
        if (stack.isEmpty()) return true;

        // CraftBukkit start
        org.bukkit.block.Block block = level.getWorld().getBlockAt(isourceblock.getPos().getX(), isourceblock.getPos().getY(), isourceblock.getPos().getZ());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(stack);

        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), CraftVector.toBukkit(banner$itemEntity.getDeltaMovement()));
        if (!BukkitFieldHooks.isEventFired()) {
            level.getCraftServer().getPluginManager().callEvent(event);
        }

        if (event.isCancelled()) {
            return false;
        }

        banner$itemEntity.setItem(CraftItemStack.asNMSCopy(event.getItem()));
        banner$itemEntity.setDeltaMovement(CraftVector.toNMS(event.getVelocity()));

        if (!dropper && !event.getItem().getType().equals(craftItem.getType())) {
            // Chain to handler for new item
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            DispenseItemBehavior idispensebehavior = (DispenseItemBehavior) DispenserBlock.DISPENSER_REGISTRY.get(eventStack.getItem());
            if (idispensebehavior != DispenseItemBehavior.NOOP && idispensebehavior.getClass() != DispenseItemBehavior.class) {
                    idispensebehavior.dispense(isourceblock, eventStack);
            } else {
                level.addFreshEntity(banner$itemEntity);
            }
            return false;
        }
        return true;
    }
}
