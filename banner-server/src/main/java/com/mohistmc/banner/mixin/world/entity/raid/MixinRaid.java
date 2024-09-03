package com.mohistmc.banner.mixin.world.entity.raid;

import com.mohistmc.banner.injection.world.entity.raid.InjectionRaid;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.raid.RaidStopEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(Raid.class)
public class MixinRaid implements InjectionRaid {

    @Shadow private Raid.RaidStatus status;

    @Shadow @Final private Map<Integer, Set<Raider>> groupRaiderMap;

    @Inject(method = "tick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/entity/raid/Raid;stop()V"),
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/Difficulty;PEACEFUL:Lnet/minecraft/world/Difficulty;")))
    public void banner$stopPeace(CallbackInfo ci) {
        CraftEventFactory.callRaidStopEvent((Raid) (Object) this, RaidStopEvent.Reason.PEACE);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;stop()V"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;isVillage(Lnet/minecraft/core/BlockPos;)Z"),
                    to = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/entity/raid/Raid;ticksActive:J")
            ))
    public void banner$stopNotInVillage(CallbackInfo ci) {
        CraftEventFactory.callRaidStopEvent((Raid) (Object) this, RaidStopEvent.Reason.NOT_IN_VILLAGE);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;stop()V"),
            slice = @Slice(
                    from = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/entity/raid/Raid;ticksActive:J"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;getTotalRaidersAlive()I")
            ))
    public void banner$stopTimeout(CallbackInfo ci) {
        CraftEventFactory.callRaidStopEvent((Raid) (Object) this, RaidStopEvent.Reason.TIMEOUT);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;stop()V"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;shouldSpawnGroup()Z"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;isStarted()Z")
            ))
    public void banner$stopUnspawnable(CallbackInfo ci) {
        CraftEventFactory.callRaidStopEvent((Raid) (Object) this, RaidStopEvent.Reason.UNSPAWNABLE);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;stop()V"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;isOver()Z")))
    public void banner$stopFinish(CallbackInfo ci) {
        CraftEventFactory.callRaidStopEvent((Raid) (Object) this, RaidStopEvent.Reason.FINISHED);
    }

    @Inject(method = "tick", at = @At(value = "FIELD", shift = At.Shift.BY, target = "Lnet/minecraft/world/entity/raid/Raid$RaidStatus;LOSS:Lnet/minecraft/world/entity/raid/Raid$RaidStatus;"))
    public void banner$finishNone(CallbackInfo ci) {
        CraftEventFactory.callRaidFinishEvent((Raid) (Object) this, new ArrayList<>());
    }

    private transient List<Player> banner$winners;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/PlayerTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;)V"))
    public void banner$addWinner(PlayerTrigger trigger, ServerPlayer player) {
        trigger.trigger(player);
        if (banner$winners == null) {
            banner$winners = new ArrayList<>();
        }
        banner$winners.add(player.getBukkitEntity());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;setDirty()V"))
    public void banner$finish(CallbackInfo ci) {
        List<Player> winners = this.banner$winners == null ? new ArrayList<>() : this.banner$winners;
        this.banner$winners = null;
        CraftEventFactory.callRaidFinishEvent((Raid) (Object) this, winners);
    }

    private transient Raider banner$leader;
    private transient List<Raider> banner$raiders;

    @Redirect(method = "spawnGroup", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;setLeader(ILnet/minecraft/world/entity/raid/Raider;)V"))
    public void banner$captureLeader(Raid raid, int raidId, Raider entity) {
        raid.setLeader(raidId, entity);
        banner$leader = entity;
    }

    @Redirect(method = "spawnGroup", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;joinRaid(ILnet/minecraft/world/entity/raid/Raider;Lnet/minecraft/core/BlockPos;Z)V"))
    public void banner$captureRaider(Raid raid, int wave, Raider entity, BlockPos pos, boolean flag) {
        raid.joinRaid(wave, entity, pos, flag);
        if (banner$raiders == null) {
            banner$raiders = new ArrayList<>();
        }
        banner$raiders.add(entity);
    }

    @Inject(method = "spawnGroup", at = @At("RETURN"))
    public void banner$spawnWave(BlockPos pos, CallbackInfo ci) {
        CraftEventFactory.callRaidSpawnWaveEvent((Raid) (Object) this, banner$leader, banner$raiders);
    }

    @Override
    public boolean isInProgress() {
        return this.status == Raid.RaidStatus.ONGOING;
    }

    @Override
    public Collection<Raider> getRaiders() {
        return this.groupRaiderMap.values().stream().flatMap(Set::stream).collect(java.util.stream.Collectors.toSet());
    }
}
