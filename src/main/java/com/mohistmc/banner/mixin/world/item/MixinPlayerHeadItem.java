package com.mohistmc.banner.mixin.world.item;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlayerHeadItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerHeadItem.class)
public abstract class MixinPlayerHeadItem extends Item {

    public MixinPlayerHeadItem(Properties properties) {
        super(properties);
    }

    @Inject(method = "verifyTagAfterLoad", at = @At("TAIL"))
    private void banner$verifyTag(CompoundTag compoundTag, CallbackInfo ci) {
        boolean banner$flag = compoundTag.contains("SkullOwner", 8)
                && !Util.isBlank(compoundTag.getString("SkullOwner"));
        // CraftBukkit start
        if (!banner$flag) {
            net.minecraft.nbt.ListTag textures =
                    compoundTag.getCompound("SkullOwner")
                    .getCompound("Properties")
                    .getList("textures", 10); // Safe due to method contracts
            for (int i = 0; i < textures.size(); i++) {
                if (textures.get(i) instanceof CompoundTag
                        && !((CompoundTag) textures.get(i)).contains("Signature", 8)
                        && ((CompoundTag) textures.get(i)).getString("Value").trim().isEmpty()) {
                    compoundTag.remove("SkullOwner");
                    break;
                }
            }
            // CraftBukkit end
        }
    }
}
