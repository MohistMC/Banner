package com.mohistmc.banner.mixin.world.entity.moster;

import com.destroystokyo.paper.event.entity.SlimeWanderEvent;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.entity.monster.Slime.SlimeKeepOnJumpingGoal")
public class MixinSlime_SlimeKeepOnJumpingGoal {

    @Shadow @Final private Slime slime;

    /**
     * @author wdog5
     * @reason paper events
     */
    @Overwrite
    public boolean canUse() {
        return !this.slime.isPassenger() && this.slime.canWander() && new SlimeWanderEvent((org.bukkit.entity.Slime) this.slime.getBukkitEntity()).callEvent(); // Paper
    }
}
