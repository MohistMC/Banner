package com.mohistmc.banner.mixin.world.level;

import com.mohistmc.banner.fabric.FabricEventFactory;
import com.mohistmc.banner.injection.world.level.InjectionLevelWriter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelWriter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelWriter.class)
public interface MixinLevelWriter extends InjectionLevelWriter {

    // CraftBukkit start
    @Override
    default boolean addFreshEntity(Entity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        return false;
    }
    // CraftBukkit end

    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void banner$addEntityEvent(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        boolean banner$result = FabricEventFactory.ADD_ENTITY_EVENT.invoker().addFreshEntity(entity);

        if (!banner$result) {
            FabricEventFactory.CANCELED_ADD_ENTITY_EVENT.invoker().canceledAddFreshEntity(entity);

            cir.setReturnValue(false);
        }
    }
}
