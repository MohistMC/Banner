package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.injection.server.level.InjectionServerPlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Scoreboard;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements InjectionServerPlayer {

    public MixinServerPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        return (CraftPlayer)super.getBukkitEntity();
    }

    @Override
    public Scoreboard getScoreboard() {
        return getBukkitEntity().getScoreboard().getHandle();
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || !getBukkitEntity().isOnline();
    }

    @Override
    public String toString() {
        return super.toString() + "(" + this.getScoreboardName() + " at " + this.getX() + "," + this.getY() + "," + this.getZ() + ")";
    }
}
