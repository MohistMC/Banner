package com.mohistmc.banner.mixin.core.server.players;

import com.google.gson.JsonObject;
import com.mohistmc.banner.injection.server.players.InjectionBanListEntry;
import net.minecraft.server.players.BanListEntry;
import net.minecraft.server.players.StoredUserEntry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Mixin(BanListEntry.class)
public abstract class MixinBanListEntry<T> extends StoredUserEntry<T> implements InjectionBanListEntry {

    @Shadow @Final protected Date created;

    @Shadow @Final public static SimpleDateFormat DATE_FORMAT;

    @Shadow @Final protected String reason;

    public void banner$constructor$super(T object) {
        throw new RuntimeException();
    }

    public void banner$constructor(T object, JsonObject jsonObject) {
        banner$constructor$super(checkExpiry(object, jsonObject));
    }

    public MixinBanListEntry(@Nullable T object) {
        super(object);
    }


    // CraftBukkit start
    private static <T> T checkExpiry(T object, JsonObject jsonobject) {
        Date expires = null;

        try {
            expires = jsonobject.has("expires") ? DATE_FORMAT.parse(jsonobject.get("expires").getAsString()) : null;
        } catch (ParseException ex) {
            // Guess we don't have a date
        }

        if (expires == null || expires.after(new Date())) {
            return object;
        } else {
            return null;
        }
    }
    // CraftBukkit end

    @Override
    public Date getCreated() {
        return this.created;
    }
}
