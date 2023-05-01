package com.mohistmc.banner.mixin.world.item.enchantment;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.bukkit.craftbukkit.v1_19_R3.enchantments.CraftEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantments.class)
public class MixinEnchantments {

    @ModifyReturnValue(method = "register", at = @At("RETURN"))
    private static Enchantment banner$modifyReturnValue(String identifier, Enchantment enchantment, CallbackInfoReturnable<Enchantment> cir) {
        enchantment = (Enchantment) Registry.register(BuiltInRegistries.ENCHANTMENT, identifier, enchantment);
        org.bukkit.enchantments.Enchantment.registerEnchantment(new CraftEnchantment(enchantment));
        return enchantment;
    }
}
