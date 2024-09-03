package com.mohistmc.banner.mixin.world.entity.moster;

import com.mohistmc.banner.injection.world.entity.monster.InjectionSlime;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(net.minecraft.world.entity.monster.Slime.class)
public abstract class MixinSlime extends Mob implements InjectionSlime {

    protected MixinSlime(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    // @formatter:off
    @Shadow public abstract int getSize();
    @Shadow public abstract EntityType<? extends net.minecraft.world.entity.monster.Slime> getType();
    // @formatter:on

    private boolean canWander = true;

    private transient List<LivingEntity> banner$slimes;

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite(remap = false)
    public void remove(Entity.RemovalReason reason) {
        int i = this.getSize();
        if (!this.level().isClientSide && i > 1 && this.isDeadOrDying()) {
            Component itextcomponent = this.getCustomName();
            boolean flag = this.isNoAi();
            float f = (float) i / 4.0F;
            int j = i / 2;
            int k = 2 + this.random.nextInt(3);

            {
                SlimeSplitEvent event = new SlimeSplitEvent((Slime) this.getBukkitEntity(), k);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled() || event.getCount() <= 0) {
                    super.remove(reason);
                    return;
                }
                k = event.getCount();
            }
            banner$slimes = new ArrayList<>(k);

            for (int l = 0; l < k; ++l) {
                float f1 = ((float) (l % 2) - 0.5F) * f;
                float f2 = ((float) (l / 2) - 0.5F) * f;
                net.minecraft.world.entity.monster.Slime slimeentity = this.getType().create(this.level());
                if (slimeentity == null) continue;
                if (this.isPersistenceRequired()) {
                    slimeentity.setPersistenceRequired();
                }

                slimeentity.setCustomName(itextcomponent);
                slimeentity.setNoAi(flag);
                slimeentity.setInvulnerable(this.isInvulnerable());
                slimeentity.setSize(j, true);
                slimeentity.moveTo(this.getX() + (double) f1, this.getY() + 0.5D, this.getZ() + (double) f2, this.random.nextFloat() * 360.0F, 0.0F);
                banner$slimes.add(slimeentity);
            }
            if (CraftEventFactory.callEntityTransformEvent((net.minecraft.world.entity.monster.Slime) (Object) this, banner$slimes, EntityTransformEvent.TransformReason.SPLIT).isCancelled()) {
                super.remove(reason);
                banner$slimes = null;
                return;
            }
            for (int l = 0; l < banner$slimes.size(); l++) {
                // Apotheosis compat
                float f1 = ((float) (l % 2) - 0.5F) * f;
                float f2 = ((float) (l / 2) - 0.5F) * f;
                net.minecraft.world.entity.monster.Slime living = (net.minecraft.world.entity.monster.Slime) banner$slimes.get(l);
                this.level().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.SLIME_SPLIT);
                this.level().addFreshEntity(living);
            }
            banner$slimes = null;
        }
        super.remove(reason);
    }

    @Inject(method = "addAdditionalSaveData",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;putInt(Ljava/lang/String;I)V"))
    private void banner$putData(CompoundTag compound, CallbackInfo ci) {
        compound.putBoolean("Paper.canWander", this.canWander); // Paper
    }

    @Inject(method = "readAdditionalSaveData",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V",
                    shift = At.Shift.AFTER))
    private void banner$readData(CompoundTag compound, CallbackInfo ci) {
        // Paper start - check exists before loading or this will be loaded as false
        if (compound.contains("Paper.canWander")) {
            this.canWander = compound.getBoolean("Paper.canWander");
        }
        // Paper end
    }

    @Override
    public boolean canWander() {
        return canWander;
    }

    @Override
    public void setWander(boolean canWander) {
        this.canWander = canWander;
    }
}
