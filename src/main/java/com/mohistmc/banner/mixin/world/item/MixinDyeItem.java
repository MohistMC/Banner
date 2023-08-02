package com.mohistmc.banner.mixin.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DyeItem.class)
public class MixinDyeItem {

    @Shadow @Final private DyeColor dyeColor;

    @Redirect(method = "interactLivingEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/Sheep;setColor(Lnet/minecraft/world/item/DyeColor;)V"))
    private void banner$cancelSetColor(Sheep instance, DyeColor dyeColor) {}

    @Inject(method = "interactLivingEntity",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$handleDyeEvent(ItemStack stack, Player player, LivingEntity interactionTarget,
                                       InteractionHand usedHand, CallbackInfoReturnable<InteractionResult> cir,
                                       Sheep sheep) {
        // CraftBukkit start
        byte bColor = (byte) this.dyeColor.getId();
        SheepDyeWoolEvent event = new SheepDyeWoolEvent((org.bukkit.entity.Sheep) sheep.getBukkitEntity(), org.bukkit.DyeColor.getByWoolData(bColor), (org.bukkit.entity.Player) player.getBukkitEntity());
        sheep.level().getCraftServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            cir.setReturnValue(InteractionResult.PASS);
        }

        sheep.setColor(DyeColor.byId((byte) event.getColor().getWoolData()));
        // CraftBukkit end
    }
}
