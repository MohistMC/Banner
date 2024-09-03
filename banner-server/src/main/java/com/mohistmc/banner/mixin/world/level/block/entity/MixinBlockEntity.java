package com.mohistmc.banner.mixin.world.level.block.entity;

import com.mohistmc.banner.injection.world.level.block.entity.InjectionBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity implements InjectionBlockEntity {

    @Shadow @Nullable public Level level;
    @Shadow @Final public BlockPos worldPosition;

    @Shadow protected abstract void applyImplicitComponents(BlockEntity.DataComponentInput dataComponentInput);

    @Shadow private DataComponentMap components;
    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();
    public CraftPersistentDataContainer persistentDataContainer;

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    public void banner$loadPersistent(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        this.persistentDataContainer = new CraftPersistentDataContainer(DATA_TYPE_REGISTRY);

        CompoundTag persistentDataTag = compoundTag.getCompound("PublicBukkitValues");
        if (persistentDataTag != null) {
            this.persistentDataContainer.putAll(persistentDataTag);
        }
    }

    @Inject(method = "saveWithoutMetadata", at = @At("RETURN"))
    private void banner$savePersistent(CallbackInfoReturnable<CompoundTag> cir) {
        if (this.persistentDataContainer != null && !this.persistentDataContainer.isEmpty()) {
            cir.getReturnValue().put("PublicBukkitValues", this.persistentDataContainer.toTagCompound());
        }
    }

    @Override
    public InventoryHolder bridge$getOwner() {
        if (this.level == null) return null;
        org.bukkit.block.Block block = CraftBlock.at(this.level, this.worldPosition);
        org.bukkit.block.BlockState state = block.getState();
        if (state instanceof InventoryHolder) return (InventoryHolder) state;
        return null;
    }

    @Override
    public CraftPersistentDataContainer bridge$persistentDataContainer() {
        return persistentDataContainer;
    }

    @Override
    public Set<DataComponentType<?>> applyComponentsSet(DataComponentMap datacomponentmap, DataComponentPatch datacomponentpatch) {
        final Set<DataComponentType<?>> set = new HashSet();
        set.add(DataComponents.BLOCK_ENTITY_DATA);
        final DataComponentMap dataComponentMap2 = PatchedDataComponentMap.fromPatch(datacomponentmap, datacomponentpatch);
        this.applyImplicitComponents(new BlockEntity.DataComponentInput() {
            @Nullable
            public <T> T get(DataComponentType<T> dataComponentType) {
                set.add(dataComponentType);
                return dataComponentMap2.get(dataComponentType);
            }

            public <T> T getOrDefault(DataComponentType<? extends T> dataComponentType, T object) {
                set.add(dataComponentType);
                return dataComponentMap2.getOrDefault(dataComponentType, object);
            }
        });
        Objects.requireNonNull(set);
        DataComponentPatch dataComponentPatch2 = datacomponentpatch.forget(set::contains);
        this.components = dataComponentPatch2.split().added();
        // CraftBukkit start
        set.remove(DataComponents.BLOCK_ENTITY_DATA); // Remove as never actually added by applyImplicitComponents
        return set;
        // CraftBukkit end
    }

    @Inject(method = "applyComponents", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$setRemove(DataComponentMap dataComponentMap, DataComponentPatch dataComponentPatch, CallbackInfo ci, Set set, DataComponentMap dataComponentMap2, DataComponentPatch dataComponentPatch2) {
        // CraftBukkit start
        set.remove(DataComponents.BLOCK_ENTITY_DATA); // Remove as never actually added by applyImplicitComponents
        // CraftBukkit end
    }
}
