package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.injection.server.level.InjectionServerPlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Scoreboard;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements InjectionServerPlayer {

   // CraftBukkit start
    public String displayName;
    public Component listName;
    public org.bukkit.Location compassTarget;
    public int newExp = 0;
    public int newLevel = 0;
    public int newTotalExp = 0;
    public boolean keepLevel = false;
    public double maxHealthCache;
    public boolean joining = true;
    public boolean sentListPacket = false;
    public Integer clientViewDistance;
    public String kickLeaveMessage = null; // SPIGOT-3034: Forward leave message to PlayerQuitEvent
    // CraftBukkit end

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

    @Override
    public Component bridge$listName() {
        return listName;
    }

    @Override
    public void banner$setListName(Component listName) {
        this.listName = listName;
    }

    @Override
    public Location bridge$compassTarget() {
        return compassTarget;
    }

    @Override
    public void banner$setCompassTarget(Location compassTarget) {
        this.compassTarget = compassTarget;
    }

    @Override
    public int bridge$newExp() {
        return newExp;
    }

    @Override
    public void banner$setNewExp(int newExp) {
        this.newExp = newExp;
    }

    @Override
    public int bridge$newLevel() {
        return newLevel;
    }

    @Override
    public void banner$setNewLevel(int newLevel) {
        this.newLevel = newLevel;
    }

    @Override
    public int bridge$newTotalExp() {
        return newTotalExp;
    }

    @Override
    public void banner$setNewTotalExp(int newTotalExp) {
        this.newTotalExp = newTotalExp;
    }

    @Override
    public boolean bridge$keepLevel() {
        return keepLevel;
    }

    @Override
    public void banner$setKeepLevel(boolean keepLevel) {
        this.keepLevel = keepLevel;
    }

    @Override
    public double bridge$maxHealthCache() {
        return maxHealthCache;
    }

    @Override
    public void banner$setMaxHealthCache(double maxHealthCache) {
        this.maxHealthCache = maxHealthCache;
    }

    @Override
    public boolean bridge$joining() {
        return joining;
    }

    @Override
    public void banner$setJoining(boolean joining) {
        this.joining = joining;
    }

    @Override
    public boolean bridge$sentListPacket() {
        return sentListPacket;
    }

    @Override
    public void banner$setSentListPacket(boolean sentListPacket) {
        this.sentListPacket = sentListPacket;
    }

    @Override
    public Integer bridge$clientViewDistance() {
        return clientViewDistance;
    }

    @Override
    public void banner$setClientViewDistance(Integer clientViewDistance) {
        this.clientViewDistance = clientViewDistance;
    }

    @Override
    public String bridge$kickLeaveMessage() {
        return kickLeaveMessage;
    }

    @Override
    public void banner$setKickLeaveMessage(String kickLeaveMessage) {
        this.kickLeaveMessage = kickLeaveMessage;
    }

    @Override
    public String bridge$displayName() {
        return displayName;
    }

    @Override
    public void banner$setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
