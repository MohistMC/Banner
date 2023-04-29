package com.mohistmc.banner.mixin.world.item;

import io.izzel.arclight.mixin.Eject;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Sheep;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DyeItem.class)
public class MixinDyeItem {

    @Shadow @Final private DyeColor dyeColor;

    @Eject(method = "interactLivingEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Sheep;setColor(Lnet/minecraft/world/item/DyeColor;)V"))
    private void banner$sheepDyeWool(net.minecraft.world.entity.animal.Sheep sheepEntity, DyeColor color, CallbackInfoReturnable<Boolean> cir, ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
        byte bColor = (byte) this.dyeColor.getId();
        SheepDyeWoolEvent event = new SheepDyeWoolEvent((Sheep) target.getBukkitEntity(), org.bukkit.DyeColor.getByWoolData(bColor), (org.bukkit.entity.Player) playerIn.getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        } else {
            sheepEntity.setColor(DyeColor.byId(event.getColor().getWoolData()));
        }
    }
}
