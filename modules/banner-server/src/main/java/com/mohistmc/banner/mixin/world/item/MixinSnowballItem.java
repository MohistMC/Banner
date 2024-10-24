package com.mohistmc.banner.mixin.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SnowballItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SnowballItem.class)
public class MixinSnowballItem extends Item {

    public MixinSnowballItem(Properties properties) {
        super(properties);
    }

    /*
    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$cancelAddEntity(Level instance, Entity entity) {
        return false;
    }

    @Inject(method = "use", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileFromRotation(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;FFF)Lnet/minecraft/world/entity/projectile/Projectile;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$addEntity(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir, ItemStack itemStack, ServerLevel serverLevel) {
        // CraftBukkit start
        if (!level.addFreshEntity(snowball)) {
            if (player instanceof ServerPlayer) {
                ((ServerPlayer) player).getBukkitEntity().updateInventory();
            }
        }
        // CraftBukkit end
    }

     */
}
