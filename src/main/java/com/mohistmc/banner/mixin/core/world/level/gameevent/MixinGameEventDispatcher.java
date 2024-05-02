package com.mohistmc.banner.mixin.core.world.level.gameevent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftGameEvent;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.event.world.GenericGameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameEventDispatcher.class)
public class MixinGameEventDispatcher {

    @Shadow @Final private ServerLevel level;

    @Inject(method = "post", cancellable = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/SectionPos;blockToSectionCoord(I)I", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$gameEvent(GameEvent gameevent, Vec3 pos, GameEvent.Context context,
                                  CallbackInfo ci, int i, BlockPos blockPos) {
        // CraftBukkit start
        GenericGameEvent event = new GenericGameEvent(CraftGameEvent.minecraftToBukkit(gameevent), CraftLocation.toBukkit(pos, level.getWorld()), (context.sourceEntity() == null) ? null : context.sourceEntity().getBukkitEntity(), i, !Bukkit.isPrimaryThread());
        level.getCraftServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
        i = event.getRadius();
        // CraftBukkit end
    }

}
