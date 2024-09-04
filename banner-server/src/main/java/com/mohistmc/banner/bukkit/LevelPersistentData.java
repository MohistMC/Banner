package com.mohistmc.banner.bukkit;

import com.mohistmc.dynamicenum.MohistDynamEnum;
import com.mojang.datafixers.DSL;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.bukkit.craftbukkit.CraftWorld;

import java.util.List;

// Banner TODO fix
public class LevelPersistentData extends SavedData {

    private CompoundTag tag;
    private static final DSL.TypeReference PDC_TYPE = () -> "bukkit_pdc";
    public static final DataFixTypes BUKKIT_PDC =
            MohistDynamEnum.addEnum(DataFixTypes.class, "BUKKIT_PDC", List.of(DSL.TypeReference.class), List.of(PDC_TYPE));

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
        return new SavedData.Factory<>(() -> new LevelPersistentData(null, null), LevelPersistentData::new, BUKKIT_PDC);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        return tag;
    }
}
