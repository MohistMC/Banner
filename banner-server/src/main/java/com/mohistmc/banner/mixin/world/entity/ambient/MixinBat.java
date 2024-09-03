package com.mohistmc.banner.mixin.world.entity.ambient;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Bat.class)
public abstract class MixinBat extends AmbientCreature {

    protected MixinBat(EntityType<? extends AmbientCreature> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract boolean isResting();

    @Shadow @Final private static TargetingConditions BAT_RESTING_TARGETING;

    @Shadow public abstract void setResting(boolean isResting);

    @Shadow @Nullable private BlockPos targetPosition;

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    protected void customServerAiStep() {
        super.customServerAiStep();
        BlockPos blockposition = this.blockPosition();
        BlockPos blockposition1 = blockposition.above();

        if (this.isResting()) {
            boolean flag = this.isSilent();

            if (this.level().getBlockState(blockposition1).isRedstoneConductor(this.level(), blockposition)) {
                if (this.random.nextInt(200) == 0) {
                    this.yHeadRot = (float) this.random.nextInt(360);
                }

                if (this.level().getNearestPlayer(BAT_RESTING_TARGETING, this) != null) {
                    // CraftBukkit Start - Call BatToggleSleepEvent
                    if (CraftEventFactory.handleBatToggleSleepEvent(this, true)) {
                        this.setResting(false);
                        if (!flag) {
                            this.level().levelEvent((Player) null, 1025, blockposition, 0);
                        }
                    }
                    // CraftBukkit End
                }
            } else {
                // CraftBukkit Start - Call BatToggleSleepEvent
                if (CraftEventFactory.handleBatToggleSleepEvent(this, true)) {
                    this.setResting(false);
                    if (!flag) {
                        this.level().levelEvent((Player) null, 1025, blockposition, 0);
                    }
                }
                // CraftBukkit End - Call BatToggleSleepEvent
            }
        } else {
            if (this.targetPosition != null && (!this.level().isEmptyBlock(this.targetPosition) || this.targetPosition.getY() <= this.level().getMinBuildHeight())) {
                this.targetPosition = null;
            }

            if (this.targetPosition == null || this.random.nextInt(30) == 0 || this.targetPosition.closerToCenterThan(this.position(), 2.0D)) {
                this.targetPosition = BlockPos.containing(this.getX() + (double) this.random.nextInt(7) - (double) this.random.nextInt(7), this.getY() + (double) this.random.nextInt(6) - 2.0D, this.getZ() + (double) this.random.nextInt(7) - (double) this.random.nextInt(7));
            }

            double d0 = (double) this.targetPosition.getX() + 0.5D - this.getX();
            double d1 = (double) this.targetPosition.getY() + 0.1D - this.getY();
            double d2 = (double) this.targetPosition.getZ() + 0.5D - this.getZ();
            Vec3 vec3d = this.getDeltaMovement();
            Vec3 vec3d1 = vec3d.add((Math.signum(d0) * 0.5D - vec3d.x) * 0.10000000149011612D, (Math.signum(d1) * 0.699999988079071D - vec3d.y) * 0.10000000149011612D, (Math.signum(d2) * 0.5D - vec3d.z) * 0.10000000149011612D);

            this.setDeltaMovement(vec3d1);
            float f = (float) (Mth.atan2(vec3d1.z, vec3d1.x) * 57.2957763671875D) - 90.0F;
            float f1 = Mth.wrapDegrees(f - this.getYRot());

            this.zza = 0.5F;
            this.setYRot(this.getYRot() + f1);
            if (this.random.nextInt(100) == 0 && this.level().getBlockState(blockposition1).isRedstoneConductor(this.level(), blockposition1)) {
                // CraftBukkit Start - Call BatToggleSleepEvent
                if (CraftEventFactory.handleBatToggleSleepEvent(this, false)) {
                    this.setResting(true);
                }
                // CraftBukkit End
            }
        }

    }


    @WrapWithCondition(method = "hurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ambient/Bat;setResting(Z)V"))
    private boolean banner$toggleSleep(Bat instance, boolean isResting) {
        // CraftBukkit Start - Call BatToggleSleepEvent
        return CraftEventFactory.handleBatToggleSleepEvent(((Bat) (Object) this), true);
        // CraftBukkit End - Call BatToggleSleepEvent
    }
}
