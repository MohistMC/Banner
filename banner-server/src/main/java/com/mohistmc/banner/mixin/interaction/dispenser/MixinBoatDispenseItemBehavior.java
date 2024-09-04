package com.mohistmc.banner.mixin.interaction.dispenser;

import com.mohistmc.banner.bukkit.BukkitFieldHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BoatDispenseItemBehavior.class)
public abstract class MixinBoatDispenseItemBehavior {

    @Shadow @Final private DefaultDispenseItemBehavior defaultDispenseItemBehavior;

    @Shadow @Final private boolean isChestBoat;

    @Shadow @Final private Boat.Type type;

    /**
     * @author wdog5
     * @reason bukkit event
     */
    @Overwrite
    public ItemStack execute(BlockSource source, ItemStack stack) {
        Direction direction = (Direction)source.state().getValue(DispenserBlock.FACING);
        Level level = source.level();
        double d = source.pos().getX() + (double)((float)direction.getStepX() * 1.125F);
        double e = source.pos().getY() + (double)((float)direction.getStepY() * 1.125F);
        double f = source.pos().getZ() + (double)((float)direction.getStepZ() * 1.125F);
        BlockPos blockPos = source.pos().relative(direction);
        double g;
        if (level.getFluidState(blockPos).is(FluidTags.WATER)) {
            g = 1.0;
        } else {
            if (!level.getBlockState(blockPos).isAir() || !level.getFluidState(blockPos.below()).is(FluidTags.WATER)) {
                return this.defaultDispenseItemBehavior.dispense(source, stack);
            }

            g = 0.0;
        }

        //Boat boat = this.isChestBoat ? new ChestBoat(level, d, e + g, f) : new Boat(level, d, e + g, f);

        // CraftBukkit start
        ItemStack itemstack1 = stack.split(1);
        org.bukkit.block.Block block = level.getWorld().getBlockAt(source.pos().getX(), source.pos().getY(), source.pos().getZ());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);

        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector(d, e + g, f));
        if (!BukkitFieldHooks.isEventFired()) {
            level.getCraftServer().getPluginManager().callEvent(event);
        }

        if (event.isCancelled()) {
            stack.grow(1);
            return stack;
        }

        if (!event.getItem().equals(craftItem)) {
            stack.grow(1);
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            DispenseItemBehavior idispensebehavior = (DispenseItemBehavior) DispenserBlock.DISPENSER_REGISTRY.get(eventStack.getItem());
            if (idispensebehavior != DispenseItemBehavior.NOOP && idispensebehavior != this) {
                idispensebehavior.dispense(source, eventStack);
                return stack;
            }
        }
        Boat boat = this.isChestBoat ? new ChestBoat(level, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ()) : new Boat(level, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ());
        ((Boat)boat).setVariant(this.type);
        ((Boat)boat).setYRot(direction.toYRot());
        if (!level.addFreshEntity((Entity) boat)) stack.grow(1); // CraftBukkit
        return stack;
    }
}
