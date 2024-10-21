package com.mohistmc.banner.mixin.world.entity.animal;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MushroomCow.class)
public abstract class MixinMushroomCow extends Cow implements Shearable, VariantHolder<MushroomCow.MushroomType> {


    public MixinMushroomCow(EntityType<? extends Cow> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "shear", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/MushroomCow;discard()V"))
    private void banner$animalTransformPre(MushroomCow mushroomCow) {
    }

    @Inject(method = "shear", cancellable = true, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$animalTransform(SoundSource source, CallbackInfo ci, @Local Cow cowEntity) {
        if (CraftEventFactory.callEntityTransformEvent((MushroomCow) (Object) this, cowEntity, EntityTransformEvent.TransformReason.SHEARED).isCancelled()) {
            ci.cancel();
        } else {
            this.level().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.SHEARED);
            this.pushRemoveCause(EntityRemoveEvent.Cause.TRANSFORMATION);
            this.discard();
        }
    }

    @Redirect(method = "shear", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$shearDrop(Level instance, Entity entity) {
        var itemEntity = (ItemEntity) entity;
        EntityDropItemEvent event = new EntityDropItemEvent(this.getBukkitEntity(), (org.bukkit.entity.Item) itemEntity.getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        return instance.addFreshEntity(entity);
    }
}
