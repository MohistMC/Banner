package com.mohistmc.banner.mixin.world.entity.animal;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(Ocelot.class)
public abstract class MixinOcelot extends Animal {

    protected MixinOcelot(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    private AtomicReference<Player> banner$player = new AtomicReference<>();

    @Inject(method = "mobInteract", at = @At(("HEAD")))
    private void banner$setPlayer(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        banner$player.set(player);
    }

    @WrapWithCondition(method = "mobInteract", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/Ocelot;setTrusting(Z)V"))
    private boolean banner$callTameEvent0(Ocelot ocelot, boolean value) {
        return !CraftEventFactory.callEntityTameEvent(((Ocelot) (Object) this), banner$player.get()).isCancelled();
    }

    @WrapWithCondition(method = "mobInteract", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/Ocelot;spawnTrustingParticles(Z)V", ordinal = 0))
    private boolean banner$callTameEvent1(Ocelot ocelot, boolean value) {
        return !CraftEventFactory.callEntityTameEvent(((Ocelot) (Object) this), banner$player.get()).isCancelled();
    }

    @WrapWithCondition(method = "mobInteract", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V", ordinal = 0))
    private boolean banner$callTameEvent2(Level level, Entity entity, byte state) {
        return !CraftEventFactory.callEntityTameEvent(((Ocelot) (Object) this), banner$player.get()).isCancelled();
    }
}
