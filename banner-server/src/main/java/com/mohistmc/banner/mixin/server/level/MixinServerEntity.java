package com.mohistmc.banner.mixin.server.level;

import com.google.common.collect.Lists;
import com.mohistmc.banner.bukkit.BukkitFieldHooks;
import com.mohistmc.banner.injection.server.level.InjectionServerEntity;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerEntity.class)
public abstract class MixinServerEntity implements InjectionServerEntity {

    // @formatter:off
    @Shadow @Final private Entity entity;
    @Shadow private List<Entity> lastPassengers;
    @Shadow @Final private Consumer<Packet<?>> broadcast;
    @Shadow private int tickCount;
    @Shadow @Final private ServerLevel level;
    @Shadow protected abstract void sendDirtyEntityData();
    @Shadow @Final private int updateInterval;
    @Shadow private int yRotp;
    @Shadow private int xRotp;
    @Shadow @Final private VecDeltaCodec positionCodec;
    @Shadow private boolean wasRiding;
    @Shadow private int teleportDelay;
    @Shadow private boolean wasOnGround;
    @Shadow @Final private boolean trackDelta;
    @Shadow private Vec3 ap;
    @Shadow private int yHeadRotp;
    @Shadow protected abstract void broadcastAndSend(Packet<?> packet);
    @Shadow @Nullable private List<SynchedEntityData.DataValue<?>> trackedDataValues;
    // @formatter:on

    @Shadow
    private static Stream<Entity> removedPassengers(List<Entity> list, List<Entity> list2) {
        return null;
    }

    @Unique
    private Set<ServerPlayerConnection> trackedPlayers;
    @Unique private int lastTick;
    @Unique private int lastUpdate, lastPosUpdate, lastMapUpdate;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(ServerLevel serverWorld, Entity entity, int updateFrequency, boolean sendVelocityUpdates, Consumer<Packet<?>> packetConsumer, CallbackInfo ci) {
        trackedPlayers = new HashSet<>();
        lastTick = BukkitFieldHooks.currentTick() - 1;
        lastUpdate = lastPosUpdate = lastMapUpdate = -1;
    }

    @Unique
    public void banner$constructor(ServerLevel serverWorld, Entity entity, int updateFrequency, boolean sendVelocityUpdates, Consumer<Packet<?>> packetConsumer) {
        throw new NullPointerException();
    }

    @Unique
    public void banner$constructor(ServerLevel serverWorld, Entity entity, int updateFrequency, boolean sendVelocityUpdates, Consumer<Packet<?>> packetConsumer, Set<ServerPlayerConnection> set) {
        banner$constructor(serverWorld, entity, updateFrequency, sendVelocityUpdates, packetConsumer);
        this.trackedPlayers = set;
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void sendChanges() {
        List<Entity> list = this.entity.getPassengers();
        if (!list.equals(this.lastPassengers)) {
            this.lastPassengers = list;
            this.broadcast.accept(new ClientboundSetPassengersPacket(this.entity));
            removedPassengers(list, this.lastPassengers).forEach((entity) -> {
                if (entity instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.teleport(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), serverPlayer.getYRot(), serverPlayer.getXRot());
                }

            });
            this.lastPassengers = list;
        }

        Entity var3 = this.entity;
        if (var3 instanceof ItemFrame itemFrame) {
            if (this.tickCount % 10 == 0) {
                ItemStack itemStack = itemFrame.getItem();
                if (itemStack.getItem() instanceof MapItem) {
                    Integer integer = MapItem.getMapId(itemStack);
                    MapItemSavedData mapItemSavedData = MapItem.getSavedData(integer, this.level);
                    if (mapItemSavedData != null) {
                        Iterator var6 = this.level.players().iterator();

                        while(var6.hasNext()) {
                            ServerPlayer serverPlayer = (ServerPlayer)var6.next();
                            mapItemSavedData.tickCarriedBy(serverPlayer, itemStack);
                            Packet<?> packet = mapItemSavedData.getUpdatePacket(integer, serverPlayer);
                            if (packet != null) {
                                serverPlayer.connection.send(packet);
                            }
                        }
                    }
                }

                this.sendDirtyEntityData();
            }
        }

        if (this.tickCount % this.updateInterval == 0 || this.entity.hasImpulse || this.entity.getEntityData().isDirty()) {
            int i;
            int j;
            if (this.entity.isPassenger()) {
                i = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
                j = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);
                boolean bl = Math.abs(i - this.yRotp) >= 1 || Math.abs(j - this.xRotp) >= 1;
                if (bl) {
                    this.broadcast.accept(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)i, (byte)j, this.entity.onGround()));
                    this.yRotp = i;
                    this.xRotp = j;
                }

                this.positionCodec.setBase(this.entity.trackingPosition());
                this.sendDirtyEntityData();
                this.wasRiding = true;
            } else {
                ++this.teleportDelay;
                i = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
                j = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);
                Vec3 vec3 = this.entity.trackingPosition();
                boolean bl2 = this.positionCodec.delta(vec3).lengthSqr() >= 7.62939453125E-6;
                Packet<?> packet2 = null;
                boolean bl3 = bl2 || this.tickCount % 60 == 0;
                boolean bl4 = Math.abs(i - this.yRotp) >= 1 || Math.abs(j - this.xRotp) >= 1;
                boolean bl5 = false;
                boolean bl6 = false;
                if (this.tickCount > 0 || this.entity instanceof AbstractArrow) {
                    long l = this.positionCodec.encodeX(vec3);
                    long m = this.positionCodec.encodeY(vec3);
                    long n = this.positionCodec.encodeZ(vec3);
                    boolean bl7 = l < -32768L || l > 32767L || m < -32768L || m > 32767L || n < -32768L || n > 32767L;
                    if (!bl7 && this.teleportDelay <= 400 && !this.wasRiding && this.wasOnGround == this.entity.onGround()) {
                        if ((!bl3 || !bl4) && !(this.entity instanceof AbstractArrow)) {
                            if (bl3) {
                                packet2 = new ClientboundMoveEntityPacket.Pos(this.entity.getId(), (short)((int)l), (short)((int)m), (short)((int)n), this.entity.onGround());
                                bl5 = true;
                            } else if (bl4) {
                                packet2 = new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)i, (byte)j, this.entity.onGround());
                                bl6 = true;
                            }
                        } else {
                            packet2 = new ClientboundMoveEntityPacket.PosRot(this.entity.getId(), (short)((int)l), (short)((int)m), (short)((int)n), (byte)i, (byte)j, this.entity.onGround());
                            bl5 = true;
                            bl6 = true;
                        }
                    } else {
                        this.wasOnGround = this.entity.onGround();
                        this.teleportDelay = 0;
                        packet2 = new ClientboundTeleportEntityPacket(this.entity);
                        bl5 = true;
                        bl6 = true;
                    }
                }

                if ((this.trackDelta || this.entity.hasImpulse || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying()) && this.tickCount > 0) {
                    Vec3 vec32 = this.entity.getDeltaMovement();
                    double d = vec32.distanceToSqr(this.ap);
                    if (d > 1.0E-7 || d > 0.0 && vec32.lengthSqr() == 0.0) {
                        this.ap = vec32;
                        this.broadcast.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
                    }
                }

                if (packet2 != null) {
                    this.broadcast.accept(packet2);
                }

                this.sendDirtyEntityData();
                if (bl5) {
                    this.positionCodec.setBase(vec3);
                }

                if (bl6) {
                    this.yRotp = i;
                    this.xRotp = j;
                }

                this.wasRiding = false;
            }

            i = Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F);
            if (Math.abs(i - this.yHeadRotp) >= 1) {
                this.broadcast.accept(new ClientboundRotateHeadPacket(this.entity, (byte)i));
                this.yHeadRotp = i;
            }

            this.entity.hasImpulse = false;
        }

        ++this.tickCount;
        if (this.entity.hurtMarked) {
            // CraftBukkit start - Create PlayerVelocity event
            boolean cancelled = false;
            if (this.entity instanceof ServerPlayer) {
                Player player = ((ServerPlayer) this.entity).getBukkitEntity();
                Vector velocity = player.getVelocity();
                PlayerVelocityEvent event = new PlayerVelocityEvent(player, velocity.clone());
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    cancelled = true;
                } else if (!velocity.equals(event.getVelocity())) {
                    player.setVelocity(event.getVelocity());
                }
            }
            if (!cancelled) {
                this.broadcastAndSend(new ClientboundSetEntityMotionPacket(this.entity));
            }
            // CraftBukkit end
            this.entity.hurtMarked = false;
        }
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void sendPairingData(ServerPlayer serverPlayer, Consumer<Packet<ClientGamePacketListener>> consumer) {
        if (this.entity.isRemoved()) {
            return;
        }
        Packet<ClientGamePacketListener> packet = this.entity.getAddEntityPacket();
        this.yHeadRotp = Mth.floor(this.entity.getYHeadRot() * 256.0f / 360.0f);
        consumer.accept(packet);
        if (this.trackedDataValues != null) {
            consumer.accept(new ClientboundSetEntityDataPacket(this.entity.getId(), this.trackedDataValues));
        }
        boolean flag = this.trackDelta;
        if (this.entity instanceof LivingEntity livingEntity) {
            Collection<AttributeInstance> collection = livingEntity.getAttributes().getSyncableAttributes();
            // CraftBukkit start - If sending own attributes send scaled health instead of current maximum health
            if (this.entity.getId() == serverPlayer.getId()) {
                ((ServerPlayer) this.entity).getBukkitEntity().injectScaledMaxHealth(collection, false);
            }
            // CraftBukkit end
            if (!collection.isEmpty()) {
                consumer.accept(new ClientboundUpdateAttributesPacket(this.entity.getId(), collection));
            }
            if (livingEntity.isFallFlying()) {
                flag = true;
            }
        }
        this.ap = this.entity.getDeltaMovement();
        if (flag && !(this.entity instanceof LivingEntity)) {
            consumer.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
        }
        if (this.entity instanceof LivingEntity) {
            List<Pair<EquipmentSlot, ItemStack>> list = Lists.newArrayList();
            EquipmentSlot[] var6 = EquipmentSlot.values();
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                EquipmentSlot equipmentSlot = var6[var8];
                ItemStack itemStack = ((LivingEntity)this.entity).getItemBySlot(equipmentSlot);
                if (!itemStack.isEmpty()) {
                    list.add(Pair.of(equipmentSlot, itemStack.copy()));
                }
            }

            if (!list.isEmpty()) {
                consumer.accept(new ClientboundSetEquipmentPacket(this.entity.getId(), list));
            }
            ((LivingEntity) this.entity).detectEquipmentUpdates(); // CraftBukkit - SPIGOT-3789: sync again immediately after sending
        }
        // CraftBukkit start - MC-109346: Fix for nonsensical head yaw
        if (this.entity instanceof ServerPlayer) {
            consumer.accept(new ClientboundRotateHeadPacket(this.entity, (byte) Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F)));
        }
        // CraftBukkit end

        if (!this.entity.getPassengers().isEmpty()) {
            consumer.accept(new ClientboundSetPassengersPacket(this.entity));
        }

        if (this.entity.isPassenger()) {
            consumer.accept(new ClientboundSetPassengersPacket(this.entity.getVehicle()));
        }

        if (this.entity instanceof Mob) {
            Mob mob = (Mob)this.entity;
            if (mob.isLeashed()) {
                consumer.accept(new ClientboundSetEntityLinkPacket(mob, mob.getLeashHolder()));
            }
        }
    }

    @Inject(method = "sendDirtyEntityData", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/server/level/ServerEntity;broadcastAndSend(Lnet/minecraft/network/protocol/Packet;)V"))
    private void banner$sendScaledHealth(CallbackInfo ci, SynchedEntityData entitydatamanager, List<SynchedEntityData.DataValue<?>> list, Set<AttributeInstance> set) {
        if (this.entity instanceof ServerPlayer player) {
            player.getBukkitEntity().injectScaledMaxHealth(set, false);
        }
    }

    @Override
    public void setTrackedPlayers(Set<ServerPlayerConnection> trackedPlayers) {
        this.trackedPlayers = trackedPlayers;
    }
}
