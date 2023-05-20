package com.mohistmc.banner.mixin.world.item.crafting;

import com.mohistmc.banner.injection.world.item.crafting.InjectionIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Ingredient.class)
public abstract class MixinIngredient implements InjectionIngredient {

    @Shadow public abstract ItemStack[] getItems();

    public boolean exact; // CraftBukkit

    @Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void banner$test(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        for (ItemStack banner$stack : this.getItems()) {
            // CraftBukkit start
            if (exact) {
                if (banner$stack.getItem() == banner$stack.getItem() && ItemStack.tagMatches(stack, banner$stack)) {
                    cir.setReturnValue(true);
                }
                continue;
            }
            if (banner$stack.is(stack.getItem())) {
                cir.setReturnValue(true);
            }
            // CraftBukkit end
        }
    }

    @Override
    public boolean bridge$exact() {
        return exact;
    }

    @Override
    public void banner$setExact(boolean exact) {
        this.exact = exact;
    }
}
