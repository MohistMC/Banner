package com.mohistmc.banner.mixin.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_20_R1.CraftEquipmentSlot;
import org.bukkit.event.player.PlayerFishEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FishingRodItem.class)
public class MixinFishingRodItem extends Item{

    public MixinFishingRodItem(Properties properties) {
        super(properties);
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$cancelEntityAdd(Level instance, Entity entity) {
        return false;
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
            shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$handleAddEntity(Level level, Player player, InteractionHand usedHand,
                                        CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
                                        ItemStack itemStack, int i, int j) {
        // CraftBukkit start
        FishingHook entityfishinghook = new FishingHook(player, level, j, i);
        PlayerFishEvent playerFishEvent = new PlayerFishEvent((org.bukkit.entity.Player) player.getBukkitEntity(), null, (org.bukkit.entity.FishHook) entityfishinghook.getBukkitEntity(), CraftEquipmentSlot.getHand(usedHand), PlayerFishEvent.State.FISHING);
        level.getCraftServer().getPluginManager().callEvent(playerFishEvent);

        if (playerFishEvent.isCancelled()) {
            player.fishing = null;
            cir.setReturnValue(InteractionResultHolder.pass(itemStack));
        }
        level.addFreshEntity(entityfishinghook);
        // CraftBukkit end
    }
}
