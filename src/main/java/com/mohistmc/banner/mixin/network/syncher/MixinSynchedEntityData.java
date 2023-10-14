package com.mohistmc.banner.mixin.network.syncher;

import com.mohistmc.banner.injection.network.syncher.InjectionSynchedEntityData;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SynchedEntityData.class)
public abstract class MixinSynchedEntityData implements InjectionSynchedEntityData {

    @Shadow protected abstract <T> SynchedEntityData.DataItem<T> getItem(EntityDataAccessor<T> key);

    @Shadow private boolean isDirty;

    @Shadow public abstract boolean isEmpty();

    @Shadow @Nullable public abstract List<SynchedEntityData.DataValue<?>> getNonDefaultValues();

    @Shadow @Final private Entity entity;

    @Override
    public <T> void markDirty(EntityDataAccessor<T> datawatcherobject) {
        this.getItem(datawatcherobject).setDirty(true);
        this.isDirty = true;
    }

    @Override
    public void refresh(ServerPlayer to) {
        if (!this.isEmpty()) {
            List<SynchedEntityData.DataValue<?>> list = this.getNonDefaultValues();

            if (list != null) {
                to.connection.send(new ClientboundSetEntityDataPacket(this.entity.getId(), list));
            }
        }
    }
}
