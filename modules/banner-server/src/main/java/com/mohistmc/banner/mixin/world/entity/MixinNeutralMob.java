package com.mohistmc.banner.mixin.world.entity;

import com.mohistmc.banner.injection.world.entity.InjectionNeutralMob;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NeutralMob.class)
public interface MixinNeutralMob extends InjectionNeutralMob {

    @Shadow void setLastHurtByMob(@Nullable LivingEntity livingEntity);

    @Shadow void setPersistentAngerTarget(@Nullable UUID persistentAngerTarget);

    @Shadow void setRemainingPersistentAngerTime(int remainingPersistentAngerTime);

    @Shadow void setTarget(@Nullable LivingEntity livingEntity);

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    default void stopBeingAngry() {
        this.setLastHurtByMob(null);
        this.setPersistentAngerTarget(null);
        this.setTarget(null, EntityTargetEvent.TargetReason.FORGOT_TARGET, true);
        this.setTarget((LivingEntity) null);
        this.setRemainingPersistentAngerTime(0);
    }

    @Override
    boolean setTarget(@Nullable LivingEntity entityliving, EntityTargetEvent.TargetReason reason, boolean fireEvent);

}
