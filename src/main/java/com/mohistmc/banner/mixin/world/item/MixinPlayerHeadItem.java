package com.mohistmc.banner.mixin.world.item;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlayerHeadItem;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerHeadItem.class)
public abstract class MixinPlayerHeadItem extends Item {

    public MixinPlayerHeadItem(Properties properties) {
        super(properties);
    }

    @Inject(method = "verifyTagAfterLoad", at = @At("TAIL"))
    private void banner$verifyTag(CompoundTag compoundTag, CallbackInfo ci) {
      if (!compoundTag.contains("SkullOwner", 8) && StringUtils.isBlank(compoundTag.getString("SkullOwner"))) {
          ListTag textures = compoundTag.getCompound("SkullOwner").getCompound("Properties").getList("textures", 10); // Safe due to method contracts
          for (net.minecraft.nbt.Tag texture : textures) {
              if (texture instanceof CompoundTag && !((CompoundTag) texture).contains("Signature", 8) && ((CompoundTag) texture).getString("Value").trim().isEmpty()) {
                  compoundTag.remove("SkullOwner");
                  break;
              }
          }
          // CraftBukkit end
      }
    }
}
