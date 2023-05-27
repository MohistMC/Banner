package com.mohistmc.banner.mixin.core.world.level.block.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BannerBlockEntity.class)
public class MixinBannerBlockEntity {

    @Shadow @Nullable public ListTag itemPatterns;

    @Inject(method = "load", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;getList(Ljava/lang/String;I)Lnet/minecraft/nbt/ListTag;",
            shift = At.Shift.AFTER))
    private void banner$checkPattern(CompoundTag tag, CallbackInfo ci) {
        if (this.itemPatterns != null) {
            // CraftBukkit start
            while (this.itemPatterns.size() > 20) {
                this.itemPatterns.remove(20);
            }
            // CraftBukkit end
        }
    }

}
