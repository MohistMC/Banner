package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(net.minecraft.world.entity.monster.Slime.class)
public abstract class MixinSlime extends Mob {

    protected MixinSlime(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    // @formatter:off
    @Shadow public abstract int getSize();
    @Shadow public abstract EntityType<? extends net.minecraft.world.entity.monster.Slime> getType();
    // @formatter:on

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite(remap = false)
    @Override
    public void remove(Entity.RemovalReason reason) {
        int i = this.getSize();
        if (!this.level.isClientSide && i > 1 && this.isDeadOrDying()) {
            Component itextcomponent = this.getCustomName();
            boolean flag = this.isNoAi();
            float f = (float) i / 4.0F;
            int j = i / 2;
            int k = 2 + this.random.nextInt(3);

            SlimeSplitEvent event = new SlimeSplitEvent((Slime) this.getBukkitEntity(), k);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled() || event.getCount() <= 0) {
                super.remove(reason);
                return;
            }
            k = event.getCount();
            List<LivingEntity> slimes = new ArrayList<>(k);

            for (int l = 0; l < k; ++l) {
                float f1 = ((float) (l % 2) - 0.5F) * f;
                float f2 = ((float) (l / 2) - 0.5F) * f;
                net.minecraft.world.entity.monster.Slime slimeentity = this.getType().create(this.level);
                if (slimeentity == null) continue;
                if (this.isPersistenceRequired()) {
                    slimeentity.setPersistenceRequired();
                }

                slimeentity.setCustomName(itextcomponent);
                slimeentity.setNoAi(flag);
                slimeentity.setInvulnerable(this.isInvulnerable());
                slimeentity.setSize(j, true);
                slimeentity.moveTo(this.getX() + (double) f1, this.getY() + 0.5D, this.getZ() + (double) f2, this.random.nextFloat() * 360.0F, 0.0F);
                slimes.add(slimeentity);
            }
            if (CraftEventFactory.callEntityTransformEvent((net.minecraft.world.entity.monster.Slime) (Object) this, slimes, EntityTransformEvent.TransformReason.SPLIT).isCancelled()) {
                super.remove(reason);
                return;
            }
            for (LivingEntity living : slimes) {
                this.level.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.SLIME_SPLIT);
                this.level.addFreshEntity(living);
            }
        }
        super.remove(reason);
    }
}
