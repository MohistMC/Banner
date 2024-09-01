package com.mohistmc.banner.mixin.world.entity.animal.goat;

import java.util.concurrent.atomic.AtomicReference;
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
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Goat.class)
public abstract class MixinGoat extends Animal {

    protected MixinGoat(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    private AtomicReference<PlayerBucketFillEvent> banner$event = new AtomicReference<>();

    @Inject(method = "mobInteract", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$bucketFillEvent(Player player, InteractionHand hand,
                                        CallbackInfoReturnable<InteractionResult> cir,
                                        ItemStack itemStack) {
        // CraftBukkit start - Got milk?
        PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent((ServerLevel) player.level(),
                player, this.blockPosition(), this.blockPosition(), null,
                itemStack, Items.MILK_BUCKET, hand);
        banner$event.set(event);
        if (event.isCancelled()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
        // CraftBukkit end
    }

    @Redirect(method = "mobInteract", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack banner$fillResult(ItemStack emptyStack, Player player, ItemStack filledStack) {
        return ItemUtils.createFilledResult(emptyStack, player, CraftItemStack.asNMSCopy(banner$event.get().getItemStack()));
    }
}
