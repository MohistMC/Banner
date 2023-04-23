package com.mohistmc.banner.mixin.world.entity.player;

import com.mohistmc.banner.injection.world.entity.player.InjectionPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftHumanEntity;
import org.spongepowered.asm.mixin.Mixin;

//TODO fix inject methods
@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity implements InjectionPlayer {

    protected MixinPlayer(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public CraftHumanEntity getBukkitEntity() {
        return (CraftHumanEntity) super.getBukkitEntity();
    }

}
