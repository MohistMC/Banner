package com.mohistmc.banner.mixin.world.level.block.entity;

import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import com.mohistmc.banner.injection.world.level.block.entity.InjectionBeaconBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R1.potion.CraftPotionUtil;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(BeaconBlockEntity.class)
public abstract class MixinBeaconBlockEntity extends BlockEntity implements InjectionBeaconBlockEntity{

    @Shadow public int levels;

    @Shadow @Nullable public MobEffect secondaryPower;

    @Shadow @Nullable public MobEffect primaryPower;

    public MixinBeaconBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "load", at = @At("RETURN"))
    public void banner$level(CompoundTag tag, CallbackInfo ci) {
        this.levels = tag.getInt("Levels");
    }

    @Inject(method = "tick",
            at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;lastCheckY:I", ordinal = 5),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void banner$activationEvent(Level level, BlockPos pos, BlockState state,
                                               BeaconBlockEntity blockEntity, CallbackInfo ci,
                                               int i, int j, int k, BlockPos blockPos,
                                               BeaconBlockEntity.BeaconBeamSection beaconBeamSection,
                                               int l, int m) {
        // Paper start - beacon activation/deactivation events
        if (m <= 0 && blockEntity.levels > 0) {
            org.bukkit.block.Block block = CraftBlock.at(level, pos);
            new io.papermc.paper.event.block.BeaconActivatedEvent(block).callEvent();
        } else if (m > 0 && blockEntity.levels <= 0) {
            org.bukkit.block.Block block = CraftBlock.at(level, pos);
            new io.papermc.paper.event.block.BeaconDeactivatedEvent(block).callEvent();
        }
        // Paper end
    }

    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void banner$beaconEvent(CallbackInfo ci) {
        // Paper start - BeaconDeactivatedEvent
        org.bukkit.block.Block block = CraftBlock.at(level, worldPosition);
        new io.papermc.paper.event.block.BeaconDeactivatedEvent(block).callEvent();
        // Paper end
    }

    @Override
    public PotionEffect getPrimaryEffect() {
        return (this.primaryPower != null) ? CraftPotionUtil.toBukkit(new MobEffectInstance(this.primaryPower, getLevel(this.levels), getAmplification(levels, primaryPower, secondaryPower), true, true)) : null;
    }

    @Override
    public PotionEffect getSecondaryEffect() {
        return (hasSecondaryEffect(levels, primaryPower, secondaryPower)) ? CraftPotionUtil.toBukkit(new MobEffectInstance(this.secondaryPower, getLevel(this.levels), getAmplification(levels, primaryPower, secondaryPower), true, true)) : null;
    }

    private static boolean hasSecondaryEffect(int i, @Nullable MobEffect mobeffectlist, @Nullable MobEffect mobeffectlist1) {
        {
            if (i >= 4 && mobeffectlist != mobeffectlist1 && mobeffectlist1 != null) {
                return true;
            }

            return false;
        }
    }


    // CraftBukkit start - split into components
    private static byte getAmplification(int i, @Nullable MobEffect mobeffectlist, @Nullable MobEffect mobeffectlist1) {
        {
            byte b0 = 0;

            if (i >= 4 && mobeffectlist == mobeffectlist1) {
                b0 = 1;
            }

            return b0;
        }
    }

    private static int getLevel(int i) {
        {
            int j = (9 + i * 2) * 20;
            return j;
        }
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    private static void applyEffects(Level pLevel, BlockPos pPos, int pLevels, @Nullable MobEffect pPrimary, @Nullable MobEffect pSecondary) {
        if (!pLevel.isClientSide && pPrimary != null) {
            double d0 = (double)(pLevels * 10 + 10);
            int i = 0;
            if (pLevels >= 4 && pPrimary == pSecondary) {
                i = 1;
            }

            int j = (9 + pLevels * 2) * 20;
            AABB aabb = (new AABB(pPos)).inflate(d0).expandTowards(0.0D, (double)pLevel.getHeight(), 0.0D);
            List<Player> list = pLevel.getEntitiesOfClass(Player.class, aabb);
            if (list.isEmpty()) return;
            for(Player player : list) {
                // Paper start - BeaconEffectEvent
                org.bukkit.block.Block block = player.level().getWorld().getBlockAt(pPos.getX(), pPos.getY(), pPos.getZ());
                PotionEffect effect = CraftPotionUtil.toBukkit(new MobEffectInstance(pPrimary, j, i, true, true));
                BeaconEffectEvent event = new BeaconEffectEvent(block, effect, (org.bukkit.entity.Player) player.getBukkitEntity(), true);
                if (CraftEventFactory.callEvent(event).isCancelled()) continue;
                // Paper end
                player.addEffect(new MobEffectInstance(CraftPotionUtil.fromBukkit(event.getEffect())), org.bukkit.event.entity.EntityPotionEffectEvent.Cause.BEACON);
            }

            if (pLevels >= 4 && pPrimary != pSecondary && pSecondary != null) {
                for(Player player1 : list) {
                    // Paper start - BeaconEffectEvent
                    org.bukkit.block.Block block = player1.level().getWorld().getBlockAt(pPos.getX(), pPos.getY(), pPos.getZ());
                    PotionEffect effect = CraftPotionUtil.toBukkit(new MobEffectInstance(pSecondary, j, 0, true, true));
                    BeaconEffectEvent event = new BeaconEffectEvent(block, effect, (org.bukkit.entity.Player) player1.getBukkitEntity(), false);
                    if (CraftEventFactory.callEvent(event).isCancelled()) continue;
                    // Paper end
                    player1.addEffect(new MobEffectInstance(CraftPotionUtil.fromBukkit(event.getEffect())), org.bukkit.event.entity.EntityPotionEffectEvent.Cause.BEACON);
                }
            }

        }

    }
    // CraftBukkit end

    private static void applyEffect(List list, MobEffect mobeffectlist, int j, int b0, boolean isPrimary, BlockPos worldPosition) { // Paper - BeaconEffectEvent
        {
            if (!list.isEmpty()) { // Paper - BeaconEffectEvent
                Iterator iterator = list.iterator();

                Player entityhuman;

                // Paper start - BeaconEffectEvent
                org.bukkit.block.Block block = ((Player) list.get(0)).level().getWorld().getBlockAt(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
                PotionEffect effect = CraftPotionUtil.toBukkit(new MobEffectInstance(mobeffectlist, j, b0, true, true));
                // Paper end
                while (iterator.hasNext()) {
                    // Paper start - BeaconEffectEvent
                    entityhuman = (ServerPlayer) iterator.next();
                    BeaconEffectEvent event = new BeaconEffectEvent(block, effect, (org.bukkit.entity.Player) entityhuman.getBukkitEntity(), isPrimary);
                    if (CraftEventFactory.callEvent(event).isCancelled()) continue;
                    entityhuman.addEffect(new MobEffectInstance(CraftPotionUtil.fromBukkit(event.getEffect())), org.bukkit.event.entity.EntityPotionEffectEvent.Cause.BEACON);
                }
            }
        }
    }
}
