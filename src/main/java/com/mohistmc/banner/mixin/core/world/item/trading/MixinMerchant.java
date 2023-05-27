package com.mohistmc.banner.mixin.core.world.item.trading;

import com.mohistmc.banner.injection.world.item.trading.InjectionMerchant;
import net.minecraft.world.item.trading.Merchant;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftMerchant;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Merchant.class)
public interface MixinMerchant extends InjectionMerchant {

    @Override
    CraftMerchant getCraftMerchant();
}
