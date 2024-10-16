package com.mohistmc.banner.mixin.network.protocol.game;

import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientboundInitializeBorderPacket.class)
public class MixinClientboundInitializeBorderPacket {

    @Redirect(method = "<init>(Lnet/minecraft/world/level/border/WorldBorder;)V",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/border/WorldBorder;getCenterX()D"))
    private double multiCenterX(WorldBorder instance) {
        // CraftBukkit start - multiply out nether border
        return  instance.getCenterX() * instance.bridge$world().dimensionType().coordinateScale();
    }

    @Redirect(method = "<init>(Lnet/minecraft/world/level/border/WorldBorder;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;getCenterZ()D"))
    private double multiCenterZ(WorldBorder instance) {
        // CraftBukkit start - multiply out nether border
        return instance.getCenterZ() * instance.bridge$world().dimensionType().coordinateScale();
    }
}
