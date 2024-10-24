package com.mohistmc.banner.mixin.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PathfinderMob.class)
public abstract class MixinPathfinderMob extends Mob {

    protected MixinPathfinderMob(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    // Banner TODO
    /*
    @Inject(method = "handleLeashAtDistance", at = @At(value = "INVOKE", target = "dropLeash"))
    private void banner$unleashDistance(CallbackInfo ci) {
        Bukkit.getPluginManager().callEvent(new EntityUnleashEvent(this.getBukkitEntity(), EntityUnleashEvent.UnleashReason.DISTANCE));
    }*/
}
