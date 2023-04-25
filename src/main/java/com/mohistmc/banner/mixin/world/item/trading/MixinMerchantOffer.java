package com.mohistmc.banner.mixin.world.item.trading;

import com.mohistmc.banner.injection.world.item.trading.InjectionMerchantOffer;
import net.minecraft.world.item.trading.MerchantOffer;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftMerchantRecipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MerchantOffer.class)
public class MixinMerchantOffer implements InjectionMerchantOffer {

    private CraftMerchantRecipe bukkitHandle;

    @Override
    public CraftMerchantRecipe asBukkit() {
        return (bukkitHandle == null) ? bukkitHandle = new CraftMerchantRecipe(((MerchantOffer) (Object) this)) : bukkitHandle;
    }
}
