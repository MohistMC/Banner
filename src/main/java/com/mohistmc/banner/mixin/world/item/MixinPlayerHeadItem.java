package com.mohistmc.banner.mixin.world.item;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.UUID;

@Mixin(PlayerHeadItem.class)
public abstract class MixinPlayerHeadItem extends Item {

    public MixinPlayerHeadItem(Properties properties) {
        super(properties);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void verifyTagAfterLoad(CompoundTag compoundTag) {
        super.verifyTagAfterLoad(compoundTag);
        if (compoundTag.contains("SkullOwner", 8) && !StringUtils.isBlank(compoundTag.getString("SkullOwner"))) {
            GameProfile gameProfile = new GameProfile((UUID)null, compoundTag.getString("SkullOwner"));
            SkullBlockEntity.updateGameprofile(gameProfile, (gameProfilex) -> {
                compoundTag.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), gameProfilex));
            });
            // CraftBukkit start
        } else {
            net.minecraft.nbt.ListTag textures = compoundTag.getCompound("SkullOwner").getCompound("Properties").getList("textures", 10); // Safe due to method contracts
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
