package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Panda.class)
public abstract class MixinPanda extends Animal {

    protected MixinPanda(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    protected static boolean canPickUpAndEat(ItemEntity itemEntity) {
        return false;
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    protected void pickUpItem(ServerLevel serverLevel, ItemEntity itemEntity) {
        boolean cancel = this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && canPickUpAndEat(itemEntity);
        if (!CraftEventFactory.callEntityPickupItemEvent((Panda) (Object) this, itemEntity, 0, cancel).isCancelled()) {
            this.onItemPickup(itemEntity);
            ItemStack itemStack = itemEntity.getItem();
            this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            this.take(itemEntity, itemStack.getCount());
            itemEntity.discard();
        }

    }
}
