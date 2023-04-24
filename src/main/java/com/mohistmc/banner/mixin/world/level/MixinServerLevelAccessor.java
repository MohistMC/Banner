package com.mohistmc.banner.mixin.world.level;


import com.mohistmc.banner.injection.world.level.InjectionServerLevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevelAccessor.class)
public interface MixinServerLevelAccessor extends LevelAccessor, InjectionServerLevelAccessor {


    @Shadow ServerLevel getLevel();

    @Override
    default void addFreshEntityWithPassengers(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        entity.getSelfAndPassengers().forEach((e) -> this.addFreshEntity(e, reason));
    }

    @Override
    default ServerLevel getMinecraftWorld() {
        return getLevel();
    }

    @Inject(method = "addFreshEntityWithPassengers", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getSelfAndPassengers()Ljava/util/stream/Stream;"))
    private void banner$addFreshEntityWithPassengers(Entity entity, CallbackInfo ci) {
        this.addFreshEntityWithPassengers(entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.DEFAULT);
    }
}
