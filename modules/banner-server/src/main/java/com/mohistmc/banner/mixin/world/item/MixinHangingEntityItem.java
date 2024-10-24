package com.mohistmc.banner.mixin.world.item;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HangingEntityItem.class)
public class MixinHangingEntityItem {

    @Inject(method = "useOn", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/HangingEntity;playPlacementSound()V"))
    public void banner$hangingPlace(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir,
                                    @Local(ordinal = 0) BlockPos blockPos, @Local Direction direction,
                                    @Local ItemStack itemStack, @Local Level world, @Local HangingEntity hangingEntity) {
        // CraftBukkit start - fire HangingPlaceEvent
        Player who = (context.getPlayer() == null) ? null : (Player) context.getPlayer().getBukkitEntity();
        org.bukkit.block.Block blockClicked = world.getWorld().getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        org.bukkit.block.BlockFace blockFace = CraftBlock.notchToBlockFace(direction);
        org.bukkit.inventory.EquipmentSlot hand = CraftEquipmentSlot.getHand(context.getHand());

        HangingPlaceEvent event = new HangingPlaceEvent((org.bukkit.entity.Hanging) ((HangingEntity) hangingEntity).getBukkitEntity(), who, blockClicked, blockFace, hand, CraftItemStack.asBukkitCopy(itemStack));
        world.getCraftServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
        // CraftBukkit end
    }
}
