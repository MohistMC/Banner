package com.mohistmc.banner.mixin.world.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ChorusFruitItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChorusFruitItem.class)
public abstract class MixinChorusFruitItem extends Item {

    public MixinChorusFruitItem(Properties properties) {
        super(properties);
    }

    @Inject(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;randomTeleport(DDDZ)Z"))
    private void banner$teleportCause(ItemStack itemStack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (livingEntity instanceof ServerPlayer p) {
             p.connection.pushTeleportCause(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT);
        }
    }
}
