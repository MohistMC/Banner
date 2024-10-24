package com.mohistmc.banner.mixin.world.item;

import com.llamalad7.mixinextras.sugar.Cancellable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderpearlItem.class)
public class MixinEnderpearlItem extends Item {

    public MixinEnderpearlItem(Properties properties) {
        super(properties);
    }


    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileFromRotation(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;FFF)Lnet/minecraft/world/entity/projectile/Projectile;"))
    private <T extends Projectile> T banner$handleEnderpearlEntity(Projectile.ProjectileFactory<T> projectileFactory, ServerLevel serverLevel, ItemStack itemStack, LivingEntity livingEntity, float f, float g, float h, @Cancellable CallbackInfoReturnable<InteractionResult> cir) {
        // CraftBukkit start
        var result = Projectile.spawnProjectileFromRotation(projectileFactory, serverLevel, itemStack, livingEntity, f, g, h);
        if (result.isRemoved()) {
            if (livingEntity instanceof ServerPlayer) {
                ((ServerPlayer) livingEntity).getBukkitEntity().updateInventory();
            }
            cir.setReturnValue(InteractionResult.FAIL);
        }
        // CraftBukkit end
        serverLevel.playSound((Player)null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (serverLevel.getRandom().nextFloat() * 0.4F + 0.8F));
        return result;
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void banner$cancelPlaySound(Level instance, Player player, double d, double e, double f, SoundEvent soundEvent, SoundSource soundSource, float g, float h) {

    }
}
