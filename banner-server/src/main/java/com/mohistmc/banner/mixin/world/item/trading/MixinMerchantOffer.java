package com.mohistmc.banner.mixin.world.item.trading;

import com.mohistmc.banner.asm.annotation.CreateConstructor;
import com.mohistmc.banner.asm.annotation.ShadowConstructor;
import com.mohistmc.banner.injection.world.item.trading.InjectionMerchantOffer;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.bukkit.craftbukkit.inventory.CraftMerchantRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantOffer.class)
public abstract class MixinMerchantOffer implements InjectionMerchantOffer {

    @Shadow public abstract ItemStack getCostA();

    @Shadow public ItemCost baseCostA;
    private CraftMerchantRecipe bukkitHandle;

    @ShadowConstructor
    public void banner$constructor(ItemCost itemCost, Optional<ItemCost> optional, ItemStack itemStack, int i, int j, int k, float f, int l) {
        throw new RuntimeException();
    }

    @CreateConstructor
    public void banner$constructor(ItemCost itemCost, Optional<ItemCost> optional, ItemStack itemStack, int i, int j, int k, float f, int l, CraftMerchantRecipe bukkit) {
        banner$constructor(itemCost, optional, itemStack, i, j, k, f, l);
        this.bukkitHandle = bukkit;
    }

    @Override
    public CraftMerchantRecipe asBukkit() {
        return (bukkitHandle == null) ? bukkitHandle = new CraftMerchantRecipe(((MerchantOffer) (Object) this)) : bukkitHandle;
    }

    @Inject(method = "getCostA", cancellable = true, at = @At("HEAD"))
    private void banner$fix(CallbackInfoReturnable<ItemStack> cir) {
        if (this.baseCostA.count() <= 0) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Redirect(method = "take", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void banner$shrink(ItemStack instance, int decrement) {
        // CraftBukkit start
        if (!this.getCostA().isEmpty()) {
            instance.shrink(this.getCostA().getCount());
        }
        // CraftBukkit end
    }
}
