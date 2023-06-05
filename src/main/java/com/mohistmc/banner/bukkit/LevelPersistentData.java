package com.mohistmc.banner.bukkit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.jetbrains.annotations.NotNull;

public class LevelPersistentData extends SavedData {

    private CompoundTag tag;

    public LevelPersistentData(CompoundTag tag) {
        this.tag = tag == null ? new CompoundTag() : tag;
    }

    public CompoundTag getTag() {
        return tag;
    }

    public void save(CraftWorld world) {
        this.tag = new CompoundTag();
        world.storeBukkitValues(this.tag);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag it) {
        return tag;
    }
}
