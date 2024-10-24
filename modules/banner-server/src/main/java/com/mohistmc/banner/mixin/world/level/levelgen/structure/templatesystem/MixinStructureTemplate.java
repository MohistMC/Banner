package com.mohistmc.banner.mixin.world.level.levelgen.structure.templatesystem;

import com.mohistmc.banner.injection.world.level.levelgen.structure.templatesystem.InjectionStructureTemplate;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureTemplate.class)
public class MixinStructureTemplate implements InjectionStructureTemplate {

    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();
    public CraftPersistentDataContainer persistentDataContainer = new CraftPersistentDataContainer(DATA_TYPE_REGISTRY);

    @Inject(method = "save", at = @At("TAIL"))
    private void banner$putBukkitValue(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        // CraftBukkit start - PDC
        if (!this.persistentDataContainer.isEmpty()) {
            tag.put("BukkitValues", this.persistentDataContainer.toTagCompound());
        }
        // CraftBukkit end
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void banner$putBukkitVar(HolderGetter<Block> blockGetter, CompoundTag tag, CallbackInfo ci) {
        // CraftBukkit start - PDC
        Tag base = tag.get("BukkitValues");
        if (base instanceof CompoundTag) {
            this.persistentDataContainer.putAll((CompoundTag) base);
        }
        // CraftBukkit end
    }
    @Override
    public CraftPersistentDataContainer bridge$persistentDataContainer() {
        return persistentDataContainer;
    }
}
