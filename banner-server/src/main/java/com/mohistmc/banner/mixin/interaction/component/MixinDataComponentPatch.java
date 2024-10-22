package com.mohistmc.banner.mixin.interaction.component;

import com.mohistmc.banner.injection.network.chat.InjectionDataComponentPatch;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.Optional;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DataComponentPatch.class)
public class MixinDataComponentPatch implements InjectionDataComponentPatch {

    @Shadow @Final private Reference2ObjectMap<DataComponentType<?>, Optional<?>> map;

    @Override
    public void copy(DataComponentPatch orig) {
        this.map.putAll(orig.map);
    }

    @Override
    public void clear(DataComponentType<?> type) {
        this.map.remove(type);
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean isSet(DataComponentType<?> type) {
        return this.map.containsKey(type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof DataComponentPatch.Builder patch) {
            return this.map.equals(patch.map);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }
}
