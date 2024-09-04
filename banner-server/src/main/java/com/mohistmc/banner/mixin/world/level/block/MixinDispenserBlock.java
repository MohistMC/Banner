package com.mohistmc.banner.mixin.world.level.block;

import com.mohistmc.banner.asm.annotation.TransformAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBlock.class)
public class MixinDispenserBlock {

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static boolean eventFired = false; // CraftBukkit

    @Inject(method = "dispenseFrom",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/DispenserBlockEntity;setItem(ILnet/minecraft/world/item/ItemStack;)V",
                    shift = At.Shift.BEFORE))
    private void banner$restEventStatus(ServerLevel serverLevel, BlockState blockState, BlockPos blockPos, CallbackInfo ci) {
        eventFired = false; // CraftBukkit - reset event status
    }

}
