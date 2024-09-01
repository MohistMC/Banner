package com.mohistmc.banner.mixin.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlayerHeadItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerHeadItem.class)
public abstract class MixinPlayerHeadItem extends Item {

    public MixinPlayerHeadItem(Properties properties) {
        super(properties);
    }

    /*
    @Inject(method = "verifyComponentsAfterLoad", at = @At("TAIL"))
    private void banner$verifyTag(ItemStack itemStack, CallbackInfo ci) {
        boolean banner$flag = components().get("SkullOwner", 8)
                && !Util.isBlank(compoundTag.getString("SkullOwner"));
        // CraftBukkit start
        if (!banner$flag) {
            net.minecraft.nbt.ListTag textures =
                    compoundTag.getCompound("SkullOwner")
                    .getCompound("Properties")
                    .getList("textures", 10); // Safe due to method contracts
            for (net.minecraft.nbt.Tag texture : textures) {
                if (texture instanceof CompoundTag
                        && !((CompoundTag) texture).contains("Signature", 8)
                        && ((CompoundTag) texture).getString("Value").trim().isEmpty()) {
                    compoundTag.remove("SkullOwner");
                    break;
                }
            }
            // CraftBukkit end
        }
    }*/
}
