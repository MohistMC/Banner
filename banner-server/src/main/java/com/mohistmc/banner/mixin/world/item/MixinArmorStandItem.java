package com.mohistmc.banner.mixin.world.item;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ArmorStandItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ArmorStandItem.class)
public class MixinArmorStandItem {

    @Inject(method = "useOn",
            at= @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V",
            shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$callEntityPlaceEvent(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir,
                                             Direction direction, Level level, BlockPlaceContext blockPlaceContext,
                                             BlockPos blockPos, ItemStack itemStack, Vec3 vec3, AABB aABB,
                                             ServerLevel serverLevel, Consumer<ArmorStand> consumer,
                                             ArmorStand armorStand, float f) {
        // CraftBukkit start
        if (CraftEventFactory.callEntityPlaceEvent(context, armorStand).isCancelled()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
        // CraftBukkit end
    }

}
