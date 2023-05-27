package com.mohistmc.banner.mixin.core.world.level.block;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBlock.class)
public class MixinDispenserBlock {

    private static boolean eventFired = BukkitExtraConstants.dispenser_eventFired; // CraftBukkit

    @Inject(method = "dispenseFrom",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/DispenserBlockEntity;setItem(ILnet/minecraft/world/item/ItemStack;)V",
                    shift = At.Shift.BEFORE))
    private void banner$restEventStatus(ServerLevel level, BlockPos pos, CallbackInfo ci) {
        eventFired = false; // CraftBukkit - reset event status
    }

}
