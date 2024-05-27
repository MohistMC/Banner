package com.mohistmc.banner.mixin.core.network.syncher;

import com.mohistmc.banner.injection.network.syncher.InjectionSynchedEntityData;
import java.util.List;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SynchedEntityData.class)
public abstract class MixinSynchedEntityData implements InjectionSynchedEntityData {

    @Shadow protected abstract <T> SynchedEntityData.DataItem<T> getItem(EntityDataAccessor<T> key);

    @Shadow private boolean isDirty;
    @Shadow @Nullable public abstract List<SynchedEntityData.DataValue<?>> getNonDefaultValues();

    @Override
    public <T> void markDirty(EntityDataAccessor<T> datawatcherobject) {
        this.getItem(datawatcherobject).setDirty(true);
        this.isDirty = true;
    }
}
