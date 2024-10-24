package com.mohistmc.banner.mixin.interaction.dispenser;

import com.mohistmc.banner.bukkit.BukkitFieldHooks;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.projectiles.CraftBlockProjectileSource;
import org.bukkit.event.block.BlockDispenseEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ProjectileDispenseBehavior.class)
public abstract class MixinProjectileDispenseBehavior {

    @Shadow @Final private ProjectileItem projectileItem;

    @Shadow @Final private ProjectileItem.DispenseConfig dispenseConfig;

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public ItemStack execute(BlockSource isourceblock, ItemStack stack) {
        Level level = isourceblock.level();
        Position position = DispenserBlock.getDispensePosition(isourceblock);
        Direction direction = (Direction)isourceblock.state().getValue(DispenserBlock.FACING);
        Projectile projectile = this.projectileItem.asProjectile(level, position, stack, direction);
        // CraftBukkit start
        //projectile.shoot((double)direction.getStepX(), (double)((float)direction.getStepY() + 0.1F), (double)direction.getStepZ(), this.getPower(), this.getUncertainty());
        ItemStack itemstack1 = stack.split(1);
        org.bukkit.block.Block block = level.getWorld().getBlockAt(isourceblock.pos().getX(), isourceblock.pos().getY(), isourceblock.pos().getZ());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);

        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new org.bukkit.util.Vector((double) direction.getStepX(), (double) ((float) direction.getStepY() + 0.1F), (double) direction.getStepZ()));
        if (!BukkitFieldHooks.isEventFired()) {
            level.getCraftServer().getPluginManager().callEvent(event);
        }

        if (event.isCancelled()) {
            stack.grow(1);
            return stack;
        }

        if (!event.getItem().equals(craftItem)) {
            stack.grow(1);
            // Chain to handler for new item
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            DispenseItemBehavior idispensebehavior = (DispenseItemBehavior) DispenserBlock.DISPENSER_REGISTRY.get(eventStack.getItem());
            if (idispensebehavior != DispenseItemBehavior.NOOP && idispensebehavior != this) {
                idispensebehavior.dispense(isourceblock, eventStack);
                return stack;
            }
        }
        projectile.shoot(event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ(), this.dispenseConfig.power(), this.dispenseConfig.uncertainty());
        projectile.banner$setProjectileSource(new CraftBlockProjectileSource((DispenserBlockEntity) isourceblock.blockEntity()));

        level.addFreshEntity(projectile);
        //stack.shrink(1);// CraftBukkit - Handled during event processing
        return stack;
    }
}
