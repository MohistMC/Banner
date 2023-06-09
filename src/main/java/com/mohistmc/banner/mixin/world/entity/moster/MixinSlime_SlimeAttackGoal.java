package com.mohistmc.banner.mixin.world.entity.moster;

import com.destroystokyo.paper.event.entity.SlimeTargetLivingEntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.entity.monster.Slime.SlimeAttackGoal")
public class MixinSlime_SlimeAttackGoal extends Goal {

    @Shadow @Final private Slime slime;

    @Shadow private int growTiredTimer;

    /**
     * @author wdog5
     * @reason paper events
     */
    @Overwrite
    public boolean canUse() {
        LivingEntity livingEntity = this.slime.getTarget();
        // Paper start
        if (livingEntity == null || !livingEntity.isAlive()) {
            return false;
        }
        if (!this.slime.canAttack(livingEntity)) {
            return false;
        }
        return this.slime.getMoveControl() instanceof Slime.SlimeMoveControl
                && this.slime.canWander()
                && new SlimeTargetLivingEntityEvent((org.bukkit.entity.Slime) this.slime.getBukkitEntity(), (org.bukkit.entity.LivingEntity) livingEntity.getBukkitEntity()).callEvent();
    }

    /**
     * @author wdog5
     * @reason paper events
     */
    @Overwrite
    public boolean canContinueToUse() {
        LivingEntity livingEntity = this.slime.getTarget();
        // Paper start
        if (livingEntity == null || !livingEntity.isAlive()) {
            return false;
        }
        if (!this.slime.canAttack(livingEntity)) {
            return false;
        }
        return --this.growTiredTimer > 0 && this.slime.canWander() && new SlimeTargetLivingEntityEvent((org.bukkit.entity.Slime) this.slime.getBukkitEntity(), (org.bukkit.entity.LivingEntity) livingEntity.getBukkitEntity()).callEvent();
        // Paper end
    }

    // Paper start - clear timer and target when goal resets
    @Override
    public void stop() {
        this.growTiredTimer = 0;
        this.slime.setTarget(null);
    }
    // Paper end
}
