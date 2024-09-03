package com.mohistmc.banner.mixin.world.entity.moster;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.bukkit.Bukkit;
import org.bukkit.entity.PigZombie;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PigZombieAngerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ZombifiedPiglin.class)
public abstract class MixinZombifiedPiglin extends Zombie {

    // @formatter:off
    @Shadow public abstract UUID getPersistentAngerTarget();
    @Shadow public abstract int getRemainingPersistentAngerTime();
    // @formatter:on

    public MixinZombifiedPiglin(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    private void alertOthers() {
        double d0 = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB axisalignedbb = AABB.unitCubeFromLowerCorner(this.position()).inflate(d0, 10.0D, d0);
        for (ZombifiedPiglin piglinEntity : this.level().getEntitiesOfClass(ZombifiedPiglin.class, axisalignedbb)) {
            if (piglinEntity != (Object) this) {
                if (piglinEntity.getTarget() == null) {
                    if (!piglinEntity.isAlliedTo(this.getTarget())) {
                        piglinEntity.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.TARGET_ATTACKED_NEARBY_ENTITY, true);
                        piglinEntity.setTarget(this.getTarget());
                    }
                }
            }
        }
    }

    @ModifyArg(method = "startPersistentAngerTimer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombifiedPiglin;setRemainingPersistentAngerTime(I)V"))
    private int banner$pigAngry(int time) {
        Entity entity = ((ServerLevel) this.level()).getEntity(this.getPersistentAngerTarget());
        PigZombieAngerEvent event = new PigZombieAngerEvent((PigZombie) this.getBukkitEntity(), entity == null ? null : entity.getBukkitEntity(), time);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return this.getRemainingPersistentAngerTime();
        }
        return event.getNewAnger();
    }
}
