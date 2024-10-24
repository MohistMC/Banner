package com.mohistmc.banner.mixin.server;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerScoreboard.class)
public class MixinServerScoreboard {

    @Shadow @Final private MinecraftServer server;

    @Redirect(method = "startTrackingObjective", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;getPlayers()Ljava/util/List;"))
    private List<ServerPlayer> banner$filterAdd(PlayerList playerList) {
        return filterPlayer(playerList.getPlayers());
    }

    @Redirect(method = "stopTrackingObjective", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;getPlayers()Ljava/util/List;"))
    private List<ServerPlayer> banner$filterRemove(PlayerList playerList) {
        return filterPlayer(playerList.getPlayers());
    }

    @Redirect(method = "*", require = 11, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void banner$sendToOwner(PlayerList playerList, Packet<?> packetIn) {
        for (ServerPlayer entity : filterPlayer(playerList.getPlayers())) {
            entity.connection.send(packetIn);
        }
    }

    private List<ServerPlayer> filterPlayer(List<ServerPlayer> list) {
        return list.stream()
                .filter(it -> it.getBukkitEntity().getScoreboard().getHandle() == (Object) this)
                .collect(Collectors.toList());
    }

    // CraftBukkit start - Send to players
    private void broadcastAll(Packet packet) {
        for (ServerPlayer entityplayer : (List<ServerPlayer>) this.server.getPlayerList().players) {
            if (entityplayer.getBukkitEntity().getScoreboard().getHandle() == ((Scoreboard) (Object) this)) {
                entityplayer.connection.send(packet);
            }
        }
    }
    // CraftBukkit end
}
