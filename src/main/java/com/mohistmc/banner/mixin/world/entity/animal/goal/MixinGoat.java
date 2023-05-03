package com.mohistmc.banner.mixin.world.entity.animal.goal;

import io.izzel.arclight.mixin.Eject;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Goat.class)
public abstract class MixinGoat extends Animal {

    protected MixinGoat(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Eject(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack banner$bucketFill(ItemStack handItem, Player player, ItemStack p_41816_, CallbackInfoReturnable<InteractionResult> cir, Player p_149379_, InteractionHand hand) {
        var event = CraftEventFactory.callPlayerBucketFillEvent((ServerLevel) player.level, player, this.blockPosition(), this.blockPosition(), null, handItem, Items.MILK_BUCKET, hand);

        if (event.isCancelled()) {
            cir.setReturnValue(InteractionResult.PASS);
            return null;
        }
        return ItemUtils.createFilledResult(handItem, player, CraftItemStack.asNMSCopy(event.getItemStack()));
    }
}
