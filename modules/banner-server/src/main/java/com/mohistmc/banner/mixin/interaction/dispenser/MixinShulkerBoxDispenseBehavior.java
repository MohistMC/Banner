package com.mohistmc.banner.mixin.interaction.dispenser;

import com.mohistmc.banner.bukkit.BukkitFieldHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.ShulkerBoxDispenseBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ShulkerBoxDispenseBehavior.class)
public class MixinShulkerBoxDispenseBehavior {

    @Inject(method = "execute",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/BlockItem;place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$dispenseEvent(BlockSource source, ItemStack stack, CallbackInfoReturnable<ItemStack> cir, Item item, Direction direction, BlockPos blockPos, Direction direction2) {
        org.bukkit.block.Block bukkitBlock = source.level().getWorld().getBlockAt(source.pos().getX(), source.pos().getY(), source.pos().getZ());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(stack);

        BlockDispenseEvent event = new BlockDispenseEvent(bukkitBlock, craftItem.clone(), new org.bukkit.util.Vector(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
        if (!BukkitFieldHooks.isEventFired()) {
            source.level().getCraftServer().getPluginManager().callEvent(event);
        }

        if (event.isCancelled()) {
            cir.setReturnValue(stack);
            return;
        }

        if (!event.getItem().equals(craftItem)) {
            // Chain to handler for new item
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            DispenseItemBehavior behavior = (DispenseItemBehavior)DispenserBlock.DISPENSER_REGISTRY.get(eventStack.getItem());
            if (behavior != DispenseItemBehavior.NOOP && behavior != this) {
                behavior.dispense(source, eventStack);
                cir.setReturnValue(stack);
            }
        }
    }
}
