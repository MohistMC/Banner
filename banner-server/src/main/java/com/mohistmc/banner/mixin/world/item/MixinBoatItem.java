package com.mohistmc.banner.mixin.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(BoatItem.class)
public abstract class MixinBoatItem extends Item {

    public MixinBoatItem(Properties properties) {
        super(properties);
    }

    @Inject(method = "use",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/BoatItem;getBoat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/HitResult;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/entity/vehicle/Boat;",
            shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$boatEvent(Level level, Player player, InteractionHand usedHand,
                                  CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
                                  ItemStack itemStack, HitResult hitResult) {
        // CraftBukkit start - Boat placement
        PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(player,
                org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK, ((BlockHitResult) hitResult).getBlockPos(),
                ((BlockHitResult) hitResult).getDirection(), itemStack,false,  usedHand, hitResult.getLocation());
        if (event.isCancelled()) {
            cir.setReturnValue(InteractionResultHolder.pass(itemStack));
        }
        // CraftBukkit end
    }

    @Redirect(method = "use",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$cancelAddEntity(Level instance, Entity entity) {
        return false;
    }

    @Inject(method = "use",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/world/phys/Vec3;)V",
            shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$handleBoatEntityAdd(Level level, Player player, InteractionHand usedHand,
                                            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
                                            ItemStack itemStack, HitResult hitResult, Vec3 vec3, double d,
                                            List<Entity> list, Boat boat) {
        // CraftBukkit start
        if (CraftEventFactory.callEntityPlaceEvent(level, ((BlockHitResult) hitResult).getBlockPos(),
                ((BlockHitResult) hitResult).getDirection(),
                player, boat, usedHand).isCancelled()) {
            cir.setReturnValue(InteractionResultHolder.fail(itemStack));
        }
        if (!level.addFreshEntity(boat)) {
            cir.setReturnValue(InteractionResultHolder.pass(itemStack));
        }
        // CraftBukkit end
    }
}
