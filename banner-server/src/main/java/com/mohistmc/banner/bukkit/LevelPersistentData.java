package com.mohistmc.banner.bukkit;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.bukkit.craftbukkit.CraftWorld;

// Banner TODO fix
public class LevelPersistentData extends SavedData {

    private CompoundTag tag;

    public LevelPersistentData(CompoundTag tag, HolderLookup.Provider provider) {
        this.tag = tag == null ? new CompoundTag() : tag;
    }

    public CompoundTag getTag() {
        return tag;
    }

    public void save(CraftWorld world) {
        this.tag = new CompoundTag();
        world.storeBukkitValues(this.tag);
    }

    public static Factory<LevelPersistentData> factory() {
        return new SavedData.Factory<>(() -> new LevelPersistentData(null, null), LevelPersistentData::new, BukkitExtraConstants.BUKKIT_PDC);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        return tag;
    }
}
