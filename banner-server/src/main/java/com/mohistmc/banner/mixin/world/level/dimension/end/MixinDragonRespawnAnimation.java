package com.mohistmc.banner.mixin.world.level.dimension.end;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net.minecraft.world.level.dimension.end.DragonRespawnAnimation$4")
public class MixinDragonRespawnAnimation {

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;discard()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$pushRevReason(ServerLevel serverLevel, EndDragonFight endDragonFight, List<EndCrystal> list, int i, BlockPos blockPos, CallbackInfo ci, Iterator var6, EndCrystal endCrystal) {
        endCrystal.pushRemoveCause(EntityRemoveEvent.Cause.EXPLODE);// CraftBukkit - add Bukkit remove cause
    }
}
