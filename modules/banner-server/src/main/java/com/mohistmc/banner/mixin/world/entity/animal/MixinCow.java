package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Cow.class)
public abstract class MixinCow extends Animal {

    protected MixinCow(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public InteractionResult mobInteract(Player playerEntity, InteractionHand hand) {
        ItemStack itemstack = playerEntity.getItemInHand(hand);
        if (itemstack.getItem() == Items.BUCKET && !this.isBaby()) {
            playerEntity.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
            org.bukkit.event.player.PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent((ServerLevel) playerEntity.level(), playerEntity, this.blockPosition(), this.blockPosition(), null, itemstack, Items.MILK_BUCKET, hand);

            if (event.isCancelled()) {
                return InteractionResult.PASS;
            }
            ItemStack itemstack1 = ItemUtils.createFilledResult(itemstack, playerEntity, CraftItemStack.asNMSCopy(event.getItemStack()));
            playerEntity.setItemInHand(hand, itemstack1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            return super.mobInteract(playerEntity, hand);
        }
    }
}
