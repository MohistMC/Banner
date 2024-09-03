package com.mohistmc.banner.mixin.world.entity.animal;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(Cat.class)
public abstract class MixinCat extends TamableAnimal {

    protected MixinCat(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    private AtomicReference<Player> bannerPlayer = new AtomicReference<>();

    @Inject(method = "mobInteract", at = @At("HEAD"))
    private void setBannerPlayer(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        bannerPlayer.set(player);
    }

    @WrapWithCondition(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Cat;tryToTame(Lnet/minecraft/world/entity/player/Player;)V"))
    private boolean banner$tameEvent(Cat cat, Player player) {
        return !CraftEventFactory.callEntityTameEvent(this, player).isCancelled(); // CraftBukkit
    }

    @WrapWithCondition(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Cat;setOrderedToSit(Z)V"))
    private boolean banner$tameEvent0(Cat cat, boolean value) {
        return !CraftEventFactory.callEntityTameEvent(this, bannerPlayer.get()).isCancelled(); // CraftBukkit
    }

    /*
    @WrapWithCondition(method = "mobInteract", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/Cat;isOwnedBy(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean banner$tameEvent1(Level level, Entity entity, byte state) {
        return !CraftEventFactory.callEntityTameEvent(this, bannerPlayer.get()).isCancelled(); // CraftBukkit
    }*/

}
