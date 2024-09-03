package com.mohistmc.banner.mixin.world.entity.moster;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.bukkit.Location;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Shulker.class)
public abstract class MixinShulker extends AbstractGolem {

    protected MixinShulker(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
    }

    // @formatter:off
    @Shadow @Nullable protected abstract Direction findAttachableSurface(BlockPos p_149811_);
    @Shadow
    public abstract void setAttachFace(Direction p_149789_);
    @Shadow @Final protected static EntityDataAccessor<Byte> DATA_PEEK_ID;
    // @formatter:on

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    protected boolean teleportSomewhere() {
        if (!this.isNoAi() && this.isAlive()) {
            BlockPos blockPos = this.blockPosition();

            for(int i = 0; i < 5; ++i) {
                BlockPos blockPos2 = blockPos.offset(Mth.randomBetweenInclusive(this.random, -8, 8), Mth.randomBetweenInclusive(this.random, -8, 8), Mth.randomBetweenInclusive(this.random, -8, 8));
                if (blockPos2.getY() > this.level().getMinBuildHeight() && this.level().isEmptyBlock(blockPos2) && this.level().getWorldBorder().isWithinBounds(blockPos2) && this.level().noCollision(this, (new AABB(blockPos2)).deflate(1.0E-6))) {
                    Direction direction = this.findAttachableSurface(blockPos2);
                    if (direction != null) {
                        // CraftBukkit start
                        EntityTeleportEvent teleport = new EntityTeleportEvent(this.getBukkitEntity(), this.getBukkitEntity().getLocation(), CraftLocation.toBukkit(blockPos2, this.level().getWorld()));
                        this.level().getCraftServer().getPluginManager().callEvent(teleport);
                        if (!teleport.isCancelled()) {
                            Location to = teleport.getTo();
                            blockPos2 = BlockPos.containing(to.getX(), to.getY(), to.getZ());
                        } else {
                            return false;
                        }
                        // CraftBukkit end
                        this.unRide();
                        this.setAttachFace(direction);
                        this.playSound(SoundEvents.SHULKER_TELEPORT, 1.0F, 1.0F);
                        this.setPos((double)blockPos2.getX() + 0.5, (double)blockPos2.getY(), (double)blockPos2.getZ() + 0.5);
                        this.level().gameEvent(GameEvent.TELEPORT, blockPos, GameEvent.Context.of(this));
                        this.entityData.set(DATA_PEEK_ID, (byte)0);
                        this.setTarget((LivingEntity)null);
                        return true;
                    }
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @Inject(method = "hitByShulkerBullet", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$breedCause(CallbackInfo ci) {
         this.level().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.BREEDING);
    }
}
