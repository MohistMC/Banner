package com.mohistmc.banner.mixin.world.entity.item;

import com.mohistmc.banner.injection.world.entity.InjectionPrimedTnt;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Explosive;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrimedTnt.class)
public abstract class MixinPrimedTnt extends Entity implements TraceableEntity, InjectionPrimedTnt {


    public MixinPrimedTnt(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    // @formatter:off
    @Shadow public abstract int getFuse();
    @Shadow public abstract void setFuse(int p_32086_);
    // @formatter:on

    public float yield;
    public boolean isIncendiary;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void banner$init(EntityType<? extends PrimedTnt> type, Level worldIn, CallbackInfo ci) {
        this.yield = 4;
        isIncendiary = false;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/entity/LivingEntity;)V", at = @At("RETURN"))
    private void banner$init(Level worldIn, double x, double y, double z, LivingEntity igniter, CallbackInfo ci) {
        this.yield = 4;
        isIncendiary = false;
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void tick() {
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
        if (this.onGround) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
        }

        int i = this.getFuse() - 1;

        this.setFuse(i);
        if (i <= 0) {
            if (!this.level().isClientSide) {
                this.explode();
            }
            this.discard();
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            if (this.level().isClientSide) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    private void explode() {
        ExplosionPrimeEvent event = new ExplosionPrimeEvent((Explosive) this.getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            this.level().explode((PrimedTnt) (Object) this, this.getX(), this.getY(0.0625), this.getZ(), event.getRadius(), event.getFire(), Level.ExplosionInteraction.TNT);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void banner$addData(CompoundTag compoundTag, CallbackInfo ci) {
        // Paper start - Try and load origin location from the old NBT tags for backwards compatibility
        if (compoundTag.contains("SourceLoc_x")) {
            int srcX = compoundTag.getInt("SourceLoc_x");
            int srcY = compoundTag.getInt("SourceLoc_y");
            int srcZ = compoundTag.getInt("SourceLoc_z");
            this.setOrigin(new org.bukkit.Location(this.level().getWorld(), srcX, srcY, srcZ));
        }
        // Paper end
    }

    @Override
    public float bridge$yield() {
        return yield;
    }

    @Override
    public void banner$setYield(float yield) {
        this.yield = yield;
    }

    @Override
    public boolean bridge$isIncendiary() {
        return isIncendiary;
    }

    @Override
    public void banner$setIsIncendiary(boolean isIncendiary) {
        this.isIncendiary = isIncendiary;
    }
}
