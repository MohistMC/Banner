package com.mohistmc.banner.mixin.world.entity.moster;

import com.destroystokyo.paper.event.entity.SlimeSwimEvent;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.entity.monster.Slime.SlimeFloatGoal")
public class MixinSlime_SlimeFloatGoal {

    @Shadow @Final private Slime slime;

    /**
     * @author wdog5
     * @reason paper events
     */
    @Overwrite
    public boolean canUse() {
        return (this.slime.isInWater() || this.slime.isInLava())
                && this.slime.getMoveControl() instanceof Slime.SlimeMoveControl
                && this.slime.canWander() && new SlimeSwimEvent((org.bukkit.entity.Slime) this.slime.getBukkitEntity()).callEvent(); // Paper
    }
}
