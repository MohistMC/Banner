package com.mohistmc.banner.mixin.world.entity.animal;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Ocelot.class)
public abstract class MixinOcelot extends Animal {

    protected MixinOcelot(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    private boolean banner$addOcelotCheck(Player player, InteractionHand hand) {
        return this.random.nextInt(3) == 0 && !CraftEventFactory.callEntityTameEvent(((Ocelot) (Object) this), player).isCancelled();
    }
}
