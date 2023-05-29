package com.mohistmc.banner.mixin.world.entity.animal;

import com.mohistmc.banner.injection.world.entity.InjectionAllay;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Allay.class)
public abstract class MixinAllay extends PathfinderMob implements InjectionAllay {


    // @formatter:off
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_CAN_DUPLICATE;
    // @formatter:on

    @Shadow protected abstract void duplicateAllay();

    protected MixinAllay(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public boolean forceDancing = false;

    @Override
    public void setCanDuplicate(boolean canDuplicate) {
        this.entityData.set(DATA_CAN_DUPLICATE, canDuplicate);
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/animal/allay/Allay;heal(F)V"))
    private void banner$healReason(CallbackInfo ci) {
        this.pushHealReason(EntityRegainHealthEvent.RegainReason.REGEN);
    }

    @Inject(method = "mobInteract", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/animal/allay/Allay;duplicateAllay()V"))
    private void banner$cancelDuplicate(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        var allay = banner$duplicate;
        banner$duplicate = null;
        if (allay == null) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    @Inject(method = "shouldStopDancing", cancellable = true, at = @At("HEAD"))
    private void banner$stopDancing(CallbackInfoReturnable<Boolean> cir) {
        if (this.forceDancing) {
            cir.setReturnValue(false);
        }
    }

    private transient Allay banner$duplicate;

    @Redirect(method = "duplicateAllay", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$captureDuplicate(Level instance, Entity entity) {
        instance.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.DUPLICATION);
        if (instance.addFreshEntity(entity)) {
            banner$duplicate = (Allay) entity;
            return true;
        }
        return false;
    }

    @Override
    public Allay duplicateAllay0() {
        try {
            this.duplicateAllay();
            return banner$duplicate;
        } finally {
            banner$duplicate = null;
        }
    }

    @Override
    public boolean bridge$forceDancing() {
        return forceDancing;
    }

    @Override
    public void banner$setForceDancing(boolean forceDancing) {
        this.forceDancing = forceDancing;
    }
}
