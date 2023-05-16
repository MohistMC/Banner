package com.mohistmc.banner.mixin.world.item;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Mixin(ArmorItem.class)
public class MixinArmorItem {

    /**
     * @author wodh5
     * @reason
     */
    @Overwrite
    public static boolean dispenseArmor(BlockSource source, ItemStack stack) {
        BlockPos blockPos = source.getPos().relative((Direction)source.getBlockState().getValue(DispenserBlock.FACING));
        List<LivingEntity> list = source.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(blockPos), EntitySelector.NO_SPECTATORS.and(new EntitySelector.MobCanWearArmorEntitySelector(stack)));
        if (list.isEmpty()) {
            return false;
        } else {
            LivingEntity livingEntity = (LivingEntity)list.get(0);
            EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(stack);
            ItemStack itemStack = stack.split(1);
            // CraftBukkit start
            Level world = source.getLevel();
            org.bukkit.block.Block block = world.getWorld().getBlockAt(source.getPos().getX(), source.getPos().getY(), source.getPos().getZ());
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemStack);

            BlockDispenseArmorEvent event = new BlockDispenseArmorEvent(block, craftItem.clone(), (CraftLivingEntity) livingEntity.getBukkitEntity());
            if (!BukkitExtraConstants.dispenser_eventFired) {
                world.getCraftServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
                stack.grow(1);
                return false;
            }

            if (!event.getItem().equals(craftItem)) {
                stack.grow(1);
                // Chain to handler for new item
                ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                DispenseItemBehavior idispensebehavior = (DispenseItemBehavior) DispenserBlock.DISPENSER_REGISTRY.get(eventStack.getItem());
                if (idispensebehavior != DispenseItemBehavior.NOOP && idispensebehavior != ArmorItem.DISPENSE_ITEM_BEHAVIOR) {
                    idispensebehavior.dispense(source, eventStack);
                    return true;
                }
            }
            livingEntity.setItemSlot(equipmentSlot,  CraftItemStack.asNMSCopy(event.getItem()));
            // CraftBukkit end
            if (livingEntity instanceof Mob) {
                ((Mob)livingEntity).setDropChance(equipmentSlot, 2.0F);
                ((Mob)livingEntity).setPersistenceRequired();
            }

            return true;
        }
    }
}
