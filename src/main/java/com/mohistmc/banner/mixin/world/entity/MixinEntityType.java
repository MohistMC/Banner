package com.mohistmc.banner.mixin.world.entity;

import com.mohistmc.banner.injection.world.entity.InjectionEntityType;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityType.class)
public abstract class MixinEntityType<T extends Entity> implements InjectionEntityType<T> {

    @Shadow @Nullable public abstract T spawn(ServerLevel level, BlockPos pos, MobSpawnType spawnType);

    @Shadow
    public static Optional<Entity> create(CompoundTag tag, Level level) {
        return Optional.empty();
    }

    @Shadow @Nullable public abstract T create(ServerLevel level, @Nullable CompoundTag nbt, @Nullable Consumer<T> consumer, BlockPos pos, MobSpawnType spawnType, boolean shouldOffsetY, boolean shouldOffsetYMore);

    @Shadow @Nullable public abstract T spawn(ServerLevel serverLevel, @Nullable ItemStack stack, @Nullable Player player, BlockPos pos, MobSpawnType spawnType, boolean shouldOffsetY, boolean shouldOffsetYMore);

    @Inject(method = "spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;",
            at = @At(value = "HEAD"))
    private void banner$spawnReason(ServerLevel serverLevel, ItemStack stack, Player player, BlockPos pos, MobSpawnType spawnType, boolean shouldOffsetY, boolean shouldOffsetYMore, CallbackInfoReturnable<T> cir) {
        CreatureSpawnEvent.SpawnReason spawnReason =  serverLevel.getAddEntityReason();
        if (spawnReason == null) {
            serverLevel.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.SPAWNER_EGG);
        }
    }

    @Inject(method = "spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/nbt/CompoundTag;Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;",
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At("RETURN"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V")))
    private void banner$returnIfSuccess(ServerLevel level, CompoundTag compound, Consumer<T> consumer, BlockPos pos, MobSpawnType spawnType, boolean shouldOffsetY, boolean shouldOffsetYMore, CallbackInfoReturnable<T> cir, T entity) {
        if (entity != null) {
            cir.setReturnValue(entity.isRemoved() ? null : entity);
        }
    }

    @Override
    public @Nullable T spawn(ServerLevel worldserver, @Nullable CompoundTag nbttagcompound, @Nullable Consumer<T> consumer, BlockPos blockposition, MobSpawnType enummobspawn, boolean flag, boolean flag1, CreatureSpawnEvent.SpawnReason spawnReason) {
        T t = this.create(worldserver, nbttagcompound, consumer, blockposition, enummobspawn, flag, flag1);
        if (t != null) {
            worldserver.pushAddEntityReason(spawnReason);
            worldserver.addFreshEntityWithPassengers(t);
            return t.isRemoved() ? null : t;
        }
        return null;
    }

    @Override
    public @Nullable T spawn(ServerLevel worldserver, BlockPos blockposition, MobSpawnType enummobspawn, CreatureSpawnEvent.SpawnReason spawnReason) {
        return this.spawn(worldserver, null, null, blockposition, enummobspawn, false, false, spawnReason);
    }
}
