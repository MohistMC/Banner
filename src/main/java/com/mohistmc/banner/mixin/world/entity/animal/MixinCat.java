package com.mohistmc.banner.mixin.world.entity.animal;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Cat.class)
public abstract class MixinCat extends TamableAnimal {

    protected MixinCat(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    private boolean banner$tameEvent(Player player, InteractionHand hand) {
        return this.random.nextInt(3) == 0 && !CraftEventFactory.callEntityTameEvent(this, player).isCancelled(); // CraftBukkit
    }

}
