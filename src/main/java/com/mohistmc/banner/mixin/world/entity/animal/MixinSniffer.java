package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.level.Level;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Sniffer.class)
public abstract class MixinSniffer extends Animal {

    protected MixinSniffer(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "dropSeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$dropSeed(ServerLevel instance, Entity entity) {
        EntityDropItemEvent event = new EntityDropItemEvent(this.getBukkitEntity(), (Item) (entity).getBukkitEntity());
        instance.getCraftServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        return instance.addFreshEntity(entity);
    }
}
