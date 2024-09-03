package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Dolphin.class)
public abstract class MixinDolphin extends WaterAnimal {

    protected MixinDolphin(EntityType<? extends WaterAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "pickUpItem", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Dolphin;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V"))
    private void banner$entityPick(ItemEntity itemEntity, CallbackInfo ci) {
        if (CraftEventFactory.callEntityPickupItemEvent((Dolphin) (Object) this, itemEntity, 0, false).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "getMaxAirSupply", cancellable = true, at = @At("RETURN"))
    private void banner$useBukkitMaxAir(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.bridge$maxAirTicks());
    }

    @Override
    public int getDefaultMaxAirSupply() {
        return Dolphin.TOTAL_AIR_SUPPLY;
    }
}
