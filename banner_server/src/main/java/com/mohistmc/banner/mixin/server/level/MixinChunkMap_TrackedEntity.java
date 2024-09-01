package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.injection.server.level.InjectionServerEntity;
import java.util.Set;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.TrackedEntity.class)
public class MixinChunkMap_TrackedEntity {


    @Shadow @Final
    ServerEntity serverEntity;

    @Shadow @Final public Set<ServerPlayerConnection> seenBy;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$setTrackedPlayers(ChunkMap outer, Entity entity, int range, int updateFrequency, boolean sendVelocityUpdates, CallbackInfo ci) {
        ((InjectionServerEntity) this.serverEntity).setTrackedPlayers(this.seenBy);
    }
}
