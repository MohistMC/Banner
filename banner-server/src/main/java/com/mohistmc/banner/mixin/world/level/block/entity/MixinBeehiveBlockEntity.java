package com.mohistmc.banner.mixin.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mohistmc.banner.injection.world.level.block.entity.InjectionBeehiveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityEnterBlockEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BeehiveBlockEntity.class)
public abstract class MixinBeehiveBlockEntity extends BlockEntity implements InjectionBeehiveBlockEntity {

    @Shadow @Final private List<BeehiveBlockEntity.BeeData> stored;

    @Shadow @Nullable public BlockPos savedFlowerPos;

    @Shadow
    protected static boolean releaseOccupant(Level level, BlockPos blockPos, BlockState blockState, BeehiveBlockEntity.Occupant occupant, @Nullable List<Entity> list, BeehiveBlockEntity.BeeReleaseStatus beeReleaseStatus, @Nullable BlockPos blockPos2) {
        return false;
    }

    public int maxBees = 3; // CraftBukkit - allow setting max amount of bees a hive can hold
    private static transient boolean banner$force;

    public MixinBeehiveBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public boolean isFull() {
        return this.stored.size() >= maxBees;
    }

    @Redirect(method = "emptyAllLivingFromHive", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Bee;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void banner$angryReason(Bee beeEntity, LivingEntity livingEntity) {
        beeEntity.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
        beeEntity.setTarget(livingEntity);
    }

    @Override
    public List<Entity> releaseBees(BlockState blockState, BeehiveBlockEntity.BeeReleaseStatus state, boolean force) {
        List<Entity> list = Lists.newArrayList();
        this.stored.removeIf(bee -> releaseBee(level, worldPosition, blockState, bee, list, state, this.savedFlowerPos, force));
        return list;
    }

    @Redirect(method = "addOccupant", at = @At(value = "INVOKE", remap = false, target = "Ljava/util/List;size()I"))
    private int banner$maxBee(List<?> list) {
        return list.size() < this.maxBees ? 1 : 3;
    }

    @Inject(method = "addOccupant", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;stopRiding()V"))
    private void banner$beeEnterBlock(Entity entity, CallbackInfo ci) {
        if (this.level != null) {
            EntityEnterBlockEvent event = new EntityEnterBlockEvent(entity.getBukkitEntity(), CraftBlock.at(this.level, this.worldPosition));
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                if (entity instanceof Bee) {
                    ((Bee) entity).setStayOutOfHiveCountdown(400);
                }
                ci.cancel();
            }
        }
    }

    private static boolean releaseBee(Level world, BlockPos pos, BlockState state, BeehiveBlockEntity.BeeData beeData, @Nullable List<Entity> list, BeehiveBlockEntity.BeeReleaseStatus status, @Nullable BlockPos pos1, boolean force) {
        banner$force = force;
        try {
            return releaseOccupant(world, pos, state, beeData.toOccupant(), list, status, pos1);
        } finally {
            banner$force = false;
        }
    }

    @Redirect(method = "releaseOccupant", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isNight()Z"))
    private static boolean banner$bypassNightCheck(Level world) {
        return !banner$force && world.isNight();
    }

    @Inject(method = "releaseOccupant", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static void banner$spawnFirst(Level level, BlockPos blockPos, BlockState blockState, BeehiveBlockEntity.Occupant occupant, List<Entity> list, BeehiveBlockEntity.BeeReleaseStatus beeReleaseStatus, BlockPos blockPos2, CallbackInfoReturnable<Boolean> cir) {
        level.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.BEEHIVE);
    }

    @Redirect(method = "releaseOccupant", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static boolean banner$addedBefore(Level world, Entity entityIn) {
        return true;
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    private void banner$readMax(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (compoundTag.contains("Bukkit.MaxEntities")) {
            this.maxBees = compoundTag.getInt("Bukkit.MaxEntities");
        }
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    private void banner$writeMax(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        compoundTag.putInt("Bukkit.MaxEntities", this.maxBees);
    }

    @Override
    public int bridge$maxBees() {
        return maxBees;
    }

    @Override
    public void banner$setMaxBees(int maxBees) {
        this.maxBees = maxBees;
    }
}
