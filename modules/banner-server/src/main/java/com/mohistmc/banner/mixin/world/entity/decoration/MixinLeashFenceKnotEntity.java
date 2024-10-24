package com.mohistmc.banner.mixin.world.entity.decoration;

import java.util.List;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LeashFenceKnotEntity.class)
public abstract class MixinLeashFenceKnotEntity extends BlockAttachedEntity {


    protected MixinLeashFenceKnotEntity(EntityType<? extends HangingEntity> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @author wdog5
     * @reason
     */
    @SuppressWarnings("ConstantConditions")
    @Overwrite
    public InteractionResult interact(final Player entityhuman, final InteractionHand enumhand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        boolean flag = false;
        final double d0 = 7.0;
        final List<Mob> list = this.level().getEntitiesOfClass(Mob.class, new AABB(this.getX() - 7.0, this.getY() - 7.0, this.getZ() - 7.0, this.getX() + 7.0, this.getY() + 7.0, this.getZ() + 7.0));
        for (final Mob entityinsentient : list) {
            if (entityinsentient.getLeashHolder() == entityhuman) {
                if (CraftEventFactory.callPlayerLeashEntityEvent(entityinsentient, (LeashFenceKnotEntity) (Object) this, entityhuman, enumhand).isCancelled()) {
                    ((ServerPlayer) entityhuman).connection.send(new ClientboundSetEntityLinkPacket(entityinsentient, entityinsentient.getLeashHolder()));
                } else {
                    entityinsentient.setLeashedTo((LeashFenceKnotEntity) (Object) this, true);
                    flag = true;
                }
            }
        }
        boolean flag1 = false;
        if (!flag) {
            boolean die = true;
            for (final Mob entityinsentient : list) {
                if (entityinsentient.isLeashed() && entityinsentient.getLeashHolder() == (Object) this) {
                    if (CraftEventFactory.callPlayerUnleashEntityEvent(entityinsentient, entityhuman, enumhand).isCancelled()) {
                        die = false;
                    } else {
                        entityinsentient.dropLeash(true, !entityhuman.getAbilities().instabuild);
                        flag1 = true;
                    }
                }
            }
            if (die) {
                this.discard();
            }
        }
        if (flag || flag1) {
            this.gameEvent(GameEvent.BLOCK_ATTACH, entityhuman);
        }
        return InteractionResult.CONSUME;
    }
}
