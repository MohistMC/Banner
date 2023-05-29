package com.mohistmc.banner.mixin.network.protocol.game;

import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundSetBorderCenterPacket.class)
public class MixinClientboundSetBorderCenterPacket {

    // @formatter:off
    @Shadow @Final @Mutable private double newCenterX;
    @Shadow @Final @Mutable private double newCenterZ;
    // @formatter:on

    @Inject(method = "<init>(Lnet/minecraft/world/level/border/WorldBorder;)V", at = @At("RETURN"))
    private void banner$nether(WorldBorder border, CallbackInfo ci) {
        this.newCenterX = border.getCenterX() * (border.bridge$world() != null ? border.bridge$world().dimensionType().coordinateScale() : 1.0);
        this.newCenterZ = border.getCenterZ() * (border.bridge$world() != null ? border.bridge$world().dimensionType().coordinateScale() : 1.0);
    }
}
