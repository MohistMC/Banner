package com.mohistmc.banner.mixin.world.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SnowballItem.class)
public class MixinSnowballItem extends Item {

    public MixinSnowballItem(Properties properties) {
        super(properties);
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$cancelAddEntity(Level instance, Entity entity) {
        return false;
    }

    @Inject(method = "use", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$addEntity(Level level, Player player, InteractionHand usedHand,
                                  CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
                                  ItemStack itemStack, Snowball snowball) {
        // CraftBukkit start
        if (!level.addFreshEntity(snowball)) {
            if (player instanceof ServerPlayer) {
                ((ServerPlayer) player).getBukkitEntity().updateInventory();
            }
        }
        // CraftBukkit end
    }
}
