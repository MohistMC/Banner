package com.mohistmc.banner.mixin.world.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecordItem.class)
public class MixinRecordItem {

    @Inject(method = "useOn",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/context/UseOnContext;getPlayer()Lnet/minecraft/world/entity/player/Player;",
            shift = At.Shift.BEFORE), cancellable = true)
    private void banner$addCheck(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValueZ() == true) cir.setReturnValue(InteractionResult.SUCCESS); // CraftBukkit - handled in ItemStack
    }
}
