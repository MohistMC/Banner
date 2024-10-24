package com.mohistmc.banner.mixin.world.item;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mohistmc.banner.bukkit.BukkitFieldHooks;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorItem.class)
public class MixinArmorItem {

    @Inject(method = "dispenseArmor",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V"), cancellable = true)
    private static void banner$inlineBukkit(BlockSource blockSource, ItemStack itemStack,
                                            CallbackInfoReturnable<Boolean> cir,
                                            @Local LivingEntity livingEntity,
                                            @Local(ordinal = 1) ItemStack itemStack2,
                                            @Share("bannerEvent") LocalRef<BlockDispenseArmorEvent> eventLocalRef) {
        // CraftBukkit start
        Level world = blockSource.level();
        org.bukkit.block.Block block = CraftBlock.at(world, blockSource.pos());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemStack2);
        BlockDispenseArmorEvent event = new BlockDispenseArmorEvent(block, craftItem.clone(), (org.bukkit.craftbukkit.entity.CraftLivingEntity) livingEntity.getBukkitEntity());
        eventLocalRef.set(event);
        if (!BukkitFieldHooks.isEventFired()) {
            world.getCraftServer().getPluginManager().callEvent(event);
        }

        if (event.isCancelled()) {
            itemStack.grow(1);
            cir.setReturnValue(false);
        }

        if (!event.getItem().equals(craftItem)) {
            itemStack.grow(1);
            // Chain to handler for new item
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            DispenseItemBehavior idispensebehavior = DispenserBlock.DISPENSER_REGISTRY.get(eventStack.getItem());
            if (idispensebehavior != DispenseItemBehavior.NOOP && idispensebehavior != ArmorItem.DISPENSE_ITEM_BEHAVIOR) {
                idispensebehavior.dispense(blockSource, eventStack);
                cir.setReturnValue(true);
            }
        }
    }

    @Redirect(method = "dispenseArmor",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V"))
    private static void banner$holdEvent(LivingEntity instance, EquipmentSlot equipmentSlot, ItemStack itemStack,  @Share("bannerEvent") LocalRef<BlockDispenseArmorEvent> eventLocalRef) {
        instance.setItemSlot(equipmentSlot, CraftItemStack.asNMSCopy(eventLocalRef.get().getItem()));
    }
}
