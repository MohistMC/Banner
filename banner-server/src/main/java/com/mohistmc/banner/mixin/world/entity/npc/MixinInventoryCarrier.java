package com.mohistmc.banner.mixin.world.entity.npc;

import com.mohistmc.banner.bukkit.BukkitContainer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(InventoryCarrier.class)
public interface MixinInventoryCarrier {

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    static void pickUpItem(Mob mob, InventoryCarrier carrier, ItemEntity itemEntity) {
        ItemStack itemstack = itemEntity.getItem();
        if (mob.wantsToPickUp(itemstack)) {
            SimpleContainer simplecontainer = carrier.getInventory();
            boolean flag = simplecontainer.canAddItem(itemstack);
            if (!flag) {
                return;
            }

            var remaining = BukkitContainer.copyOf(carrier.getInventory()).addItem(itemstack);
            if (CraftEventFactory.callEntityPickupItemEvent(mob, itemEntity, remaining.getCount(), false).isCancelled()) {
                return;
            }
            mob.onItemPickup(itemEntity);
            int i = itemstack.getCount();
            ItemStack itemstack1 = simplecontainer.addItem(itemstack);
            mob.take(itemEntity, i - itemstack1.getCount());
            if (itemstack1.isEmpty()) {
                itemEntity.discard();
            } else {
                itemstack.setCount(itemstack1.getCount());
            }
        }

    }
}
