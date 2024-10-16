package com.mohistmc.banner.stackdeobf.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

public final class CompatUtil {

    private static final JsonObject VERSION_DATA;

    static {
        try (InputStream input = CompatUtil.class.getResourceAsStream("/version.json");
             Reader reader = new InputStreamReader(Objects.requireNonNull(input, "version.json not found"))) {
            // can't use mojangs utility class GsonHelper, this would load ResourceLocation which breaks some
            // mods injecting into that class, this class is loaded before any mixins are applied
            VERSION_DATA = new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    // get data directly from version.json, loading SharedConstants would result
    // in classes being loaded before mixins are applied
    public static final String VERSION_ID = VERSION_DATA.get("id").getAsString();
    public static final String VERSION_NAME = VERSION_DATA.get("name").getAsString();
    public static final int WORLD_VERSION = VERSION_DATA.get("world_version").getAsInt();

    private CompatUtil() {
    }
}
