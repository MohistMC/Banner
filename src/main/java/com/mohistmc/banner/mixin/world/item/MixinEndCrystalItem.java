package com.mohistmc.banner.mixin.world.item;

import com.mohistmc.banner.util.DistValidate;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.context.UseOnContext;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystalItem.class)
public class MixinEndCrystalItem {

    private transient EndCrystal banner$enderCrystalEntity;

    @Redirect(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;setShowBottom(Z)V"))
    public void banner$captureEntity(EndCrystal enderCrystalEntity, boolean showBottom) {
        banner$enderCrystalEntity = enderCrystalEntity;
        enderCrystalEntity.setShowBottom(showBottom);
    }

    @Inject(method = "useOn", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    public void banner$entityPlace(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (DistValidate.isValid(context) && CraftEventFactory.callEntityPlaceEvent(context, banner$enderCrystalEntity).isCancelled()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
        banner$enderCrystalEntity = null;
    }

}
