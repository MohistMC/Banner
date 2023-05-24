package com.mohistmc.banner.mixin.world.item.trading;

import com.mohistmc.banner.injection.world.item.trading.InjectionMerchantOffer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftMerchantRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantOffer.class)
public abstract class MixinMerchantOffer implements InjectionMerchantOffer {

    // @formatter:off
    @Shadow public ItemStack baseCostA;
    // @formatter:on

    @Shadow
    private int demand;

    @Shadow public abstract ItemStack getCostA();

    private CraftMerchantRecipe bukkitHandle;

    public void banner$constructor(ItemStack buyingStackFirstIn, ItemStack buyingStackSecondIn, ItemStack sellingStackIn, int usesIn, int maxUsesIn, int givenEXPIn, float priceMultiplierIn, int demand) {
        throw new RuntimeException();
    }

    public void banner$constructor(ItemStack buyingStackFirstIn, ItemStack buyingStackSecondIn, ItemStack sellingStackIn, int usesIn, int maxUsesIn, int givenEXPIn, float priceMultiplierIn, int demand, CraftMerchantRecipe bukkit) {
        banner$constructor(buyingStackFirstIn, buyingStackSecondIn, sellingStackIn, usesIn, maxUsesIn, givenEXPIn, priceMultiplierIn, demand);
        this.bukkitHandle = bukkit;
    }

    @Override
    public CraftMerchantRecipe asBukkit() {
        return (bukkitHandle == null) ? bukkitHandle = new CraftMerchantRecipe(((MerchantOffer) (Object) this)) : bukkitHandle;
    }

    @Inject(method = "getCostA", cancellable = true, at = @At("HEAD"))
    private void banner$fix(CallbackInfoReturnable<ItemStack> cir) {
        if (this.baseCostA.getCount() <= 0) {
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
