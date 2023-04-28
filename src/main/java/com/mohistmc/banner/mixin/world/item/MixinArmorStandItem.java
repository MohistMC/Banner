package com.mohistmc.banner.mixin.world.item;

import com.mohistmc.banner.util.DistValidate;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ArmorStandItem;
import net.minecraft.world.item.context.UseOnContext;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandItem.class)
public class MixinArmorStandItem {

    private transient ArmorStand banner$entity;

    @Redirect(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;moveTo(DDDFF)V"))
    public void banner$captureEntity(ArmorStand armorStandEntity, double x, double y, double z, float yaw, float pitch) {
        armorStandEntity.moveTo(x, y, z, yaw, pitch);
        banner$entity = armorStandEntity;
    }

    @Inject(method = "useOn", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    public void banner$entityPlace(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (DistValidate.isValid(context) && CraftEventFactory.callEntityPlaceEvent(context, banner$entity).isCancelled()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
        banner$entity = null;
    }
}
