package com.mohistmc.banner.mixin.core.server.players;

import com.google.gson.JsonObject;
import com.mohistmc.banner.asm.annotation.CreateConstructor;
import com.mohistmc.banner.asm.annotation.ShadowConstructor;
import com.mohistmc.banner.injection.server.players.InjectionBanListEntry;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraft.server.players.BanListEntry;
import net.minecraft.server.players.StoredUserEntry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BanListEntry.class)
public abstract class MixinBanListEntry<T> extends StoredUserEntry<T> implements InjectionBanListEntry {

    // @formatter:off
    @Shadow @Final protected Date created;

    public MixinBanListEntry(@Nullable T object) {
        super(object);
    }
    // @formatter:on

    public Date getCreated() {
        return this.created;
    }
}
