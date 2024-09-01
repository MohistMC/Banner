package com.mohistmc.banner.mixin.server.players;

import java.util.Iterator;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SleepStatus.class)
public abstract class MixinSleepStatus {

    @Shadow private int activePlayers;

    @Shadow private int sleepingPlayers;

    @Shadow public abstract int sleepersNeeded(int requiredSleepPercentage);

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public boolean areEnoughDeepSleeping(int requiredSleepPercentage, List<ServerPlayer> sleepingPlayers) {
        // CraftBukkit start
        int j = (int) sleepingPlayers.stream().filter((eh) -> { return eh.isSleepingLongEnough() || eh.bridge$fauxSleeping(); }).count();
        boolean anyDeepSleep = sleepingPlayers.stream().anyMatch(Player::isSleepingLongEnough);
        return anyDeepSleep && j >= this.sleepersNeeded(j);
        // CraftBukkit end
    }

    /**
     * @author wdog5
     * @reason  bukkit
     */
    @Overwrite
    public boolean update(List<ServerPlayer> list) {
        int i = this.activePlayers;
        int j = this.sleepingPlayers;

        this.activePlayers = 0;
        this.sleepingPlayers = 0;
        Iterator<ServerPlayer> iterator = list.iterator();
        boolean anySleep = false; // CraftBukkit

        while (iterator.hasNext()) {
            ServerPlayer entityplayer = (ServerPlayer) iterator.next();

            if (!entityplayer.isSpectator()) {
                ++this.activePlayers;
                if (entityplayer.isSleeping() || entityplayer.bridge$fauxSleeping()) { // CraftBukkit
                    ++this.sleepingPlayers;
                }
                // CraftBukkit start
                if (entityplayer.isSleeping()) {
                    anySleep = true;
                }
                // CraftBukkit end
            }
        }

        return anySleep && (j > 0 || this.sleepingPlayers > 0) && (i != this.activePlayers || j != this.sleepingPlayers); // CraftBukkit
    }

}
