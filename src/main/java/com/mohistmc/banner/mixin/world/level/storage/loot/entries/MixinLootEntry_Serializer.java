package com.mohistmc.banner.mixin.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LootPoolEntryContainer.Serializer.class)
public abstract class MixinLootEntry_Serializer<T extends LootPoolEntryContainer> {

    @Shadow public abstract void serialize(JsonObject json, T value, JsonSerializationContext serializationContext);

    @Shadow public abstract T deserialize(JsonObject json, JsonDeserializationContext serializationContext);

    public final void a(JsonObject object, T t0, JsonSerializationContext context) {
        this.serialize(object, t0, context);
    }

    public final T a(JsonObject object, JsonDeserializationContext context) {
        return this.deserialize(object, context);
    }
}
