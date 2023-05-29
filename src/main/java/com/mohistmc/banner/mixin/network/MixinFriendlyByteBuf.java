package com.mohistmc.banner.mixin.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FriendlyByteBuf.class)
public abstract class MixinFriendlyByteBuf {


    @Shadow public abstract ByteBuf writeBoolean(boolean bl);

    @Inject(method = "writeItem", at = @At(value = "HEAD"))
    private void modifyReturn(ItemStack stack, CallbackInfoReturnable<FriendlyByteBuf> cir) {
        if (stack.getItem() == null) {
            this.writeBoolean(false);
        }
    }

    @Inject(method = "readItem",
            at = @At(value = "TAIL"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$setItemMeta(CallbackInfoReturnable<ItemStack> cir, Item item, int i, ItemStack itemStack) {
        // CraftBukkit start
        if (itemStack.getTag() != null) {
            CraftItemStack.setItemMeta(itemStack, CraftItemStack.getItemMeta(itemStack));
        }
        // CraftBukkit end
    }
}
