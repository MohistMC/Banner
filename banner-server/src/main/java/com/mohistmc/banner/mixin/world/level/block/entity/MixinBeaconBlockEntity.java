package com.mohistmc.banner.mixin.world.level.block.entity;

import com.mohistmc.banner.asm.annotation.TransformAccess;
import com.mohistmc.banner.injection.world.level.block.entity.InjectionBeaconBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

// Banner TODO fix patches
@Mixin(BeaconBlockEntity.class)
public abstract class MixinBeaconBlockEntity extends BlockEntity implements InjectionBeaconBlockEntity{

    @Shadow public int levels;

    public MixinBeaconBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    public void banner$level(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        this.levels = compoundTag.getInt("Levels");
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

    /*
    @Override
    public PotionEffect getPrimaryEffect() {
        return (this.primaryPower != null) ? CraftPotionUtil.toBukkit(new MobEffectInstance(this.primaryPower, getLevel(this.levels), getAmplification(levels, primaryPower, secondaryPower), true, true)) : null;
    }

    @Override
    public PotionEffect getSecondaryEffect() {
        return (hasSecondaryEffect(levels, primaryPower, secondaryPower)) ? CraftPotionUtil.toBukkit(new MobEffectInstance(this.secondaryPower, getLevel(this.levels), getAmplification(levels, primaryPower, secondaryPower), true, true)) : null;
    }*/

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

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static List getHumansInRange(Level world, BlockPos blockposition, int i) {
        {
            double d0 = (double) (i * 10 + 10);

            AABB axisalignedbb = (new AABB(blockposition)).inflate(d0).expandTowards(0.0D, (double) world.getHeight(), 0.0D);
            List<Player> list = world.getEntitiesOfClass(Player.class, axisalignedbb);

            return list;
        }
    }
}
