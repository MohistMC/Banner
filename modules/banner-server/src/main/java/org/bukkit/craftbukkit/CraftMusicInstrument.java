package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Instrument;
import org.bukkit.MusicInstrument;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.util.Handleable;
import org.jetbrains.annotations.NotNull;

public class CraftMusicInstrument extends MusicInstrument implements Handleable<Instrument> {

    public static MusicInstrument minecraftToBukkit(Instrument minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, Registries.INSTRUMENT, Registry.INSTRUMENT);
    }

    public static MusicInstrument minecraftHolderToBukkit(Holder<Instrument> minecraft) {
        return CraftMusicInstrument.minecraftToBukkit(minecraft.value());
    }

    public static Instrument bukkitToMinecraft(MusicInstrument bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    public static Holder<Instrument> bukkitToMinecraftHolder(MusicInstrument bukkit) {
        Preconditions.checkArgument(bukkit != null);

        net.minecraft.core.Registry<Instrument> registry = CraftRegistry.getMinecraftRegistry(Registries.INSTRUMENT);

        if (registry.wrapAsHolder(CraftMusicInstrument.bukkitToMinecraft(bukkit)) instanceof Holder.Reference<Instrument> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own instrument without properly registering it.");
    }

    public static String bukkitToString(MusicInstrument bukkit) {
        Preconditions.checkArgument(bukkit != null);

        return bukkit.getKey().toString();
    }

    public static MusicInstrument stringToBukkit(String string) {
        Preconditions.checkArgument(string != null);

        return Registry.INSTRUMENT.get(NamespacedKey.fromString(string));
    }

    private final NamespacedKey key;
    private final Instrument handle;

    public CraftMusicInstrument(NamespacedKey key, Instrument handle) {
        this.key = key;
        this.handle = handle;
    }

    @Override
    public Instrument getHandle() {
        return this.handle;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return this.key;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof CraftMusicInstrument)) {
            return false;
        }

        return this.getKey().equals(((MusicInstrument) other).getKey());
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }

    @Override
    public String toString() {
        return "CraftMusicInstrument{key=" + this.key + "}";
    }
}
