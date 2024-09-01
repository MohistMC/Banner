package com.mohistmc.banner.mixin.world.item;

import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CrossbowItem.class)
public class MixinCrossbowItem {

    private static AtomicBoolean banner$capturedBoolean = new AtomicBoolean(true);

    /*
    @Inject(method = "shootProjectile", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;shoot(DDDFF)V"))
    private void banner$entityShoot(LivingEntity livingEntity, Projectile projectile, int i, float f, float g, float h, LivingEntity livingEntity2, CallbackInfo ci) {
        if (!DistValidate.isValid(worldIn)) {
            banner$capturedBoolean.set(true);
            return;
        }
        EntityShootBowEvent event = CraftEventFactory.callEntityShootBowEvent(shooter, crossbow, projectile, proj, shooter.getUsedItemHand(), soundPitch, true);
        if (event.isCancelled()) {
            event.getProjectile().remove();
            ci.cancel();
        }
        banner$capturedBoolean.set(event.getProjectile() == proj.getBukkitEntity());
    }

    @Eject(method = "shootProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static boolean banner$addEntity(Level world, Entity entityIn, CallbackInfo ci, Level worldIn, LivingEntity shooter) {
        if (banner$capturedBoolean.get()) {
            if (!world.addFreshEntity(entityIn)) {
                if (shooter instanceof ServerPlayer) {
                    ((ServerPlayer) shooter).getBukkitEntity().updateInventory();
                }
                ci.cancel();
            }
        }
        return true;
    }*/
}
