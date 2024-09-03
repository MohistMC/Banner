package com.mohistmc.banner.mixin.world.item;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ArmorItem.class)
public class MixinArmorItem {

    private static AtomicReference<BlockDispenseArmorEvent> banner$armorEvent = new AtomicReference<>();

    @Inject(method = "dispenseArmor",
            at= @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V",
            shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void banner$callArmorEvent(BlockSource source, ItemStack stack, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos, List list, LivingEntity livingEntity, EquipmentSlot equipmentSlot, ItemStack itemStack) {
        // CraftBukkit start
        Level world = source.level();
        org.bukkit.block.Block block = world.getWorld().getBlockAt(source.pos().getX(), source.pos().getY(), source.pos().getZ());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemStack);

        BlockDispenseArmorEvent event = new BlockDispenseArmorEvent(block, craftItem.clone(), (CraftLivingEntity) livingEntity.getBukkitEntity());
        banner$armorEvent.set(event);
        if (!BukkitExtraConstants.dispenser_eventFired) {
            world.getCraftServer().getPluginManager().callEvent(event);
        }

        if (event.isCancelled()) {
            stack.grow(1);
            cir.setReturnValue(false);
        }

        if (!event.getItem().equals(craftItem)) {
            stack.grow(1);
            // Chain to handler for new item
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            DispenseItemBehavior idispensebehavior = (DispenseItemBehavior) DispenserBlock.DISPENSER_REGISTRY.get(eventStack.getItem());
            if (idispensebehavior != DispenseItemBehavior.NOOP && idispensebehavior != ArmorItem.DISPENSE_ITEM_BEHAVIOR) {
                idispensebehavior.dispense(source, eventStack);
                cir.setReturnValue(true);
            }
        }
        // CraftBukkit end
    }

    @ModifyArg(method = "dispenseArmor", at= @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V"),
            index = 1)
    private static ItemStack banner$setStack(ItemStack stack) {
        return CraftItemStack.asNMSCopy(banner$armorEvent.get().getItem());
    }
}
