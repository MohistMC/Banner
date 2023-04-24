package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.injection.server.level.InjectionServerChunkCache;
import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;

// TODO fix inject method
@Mixin(ServerChunkCache.class)
public class MixinServerChunkCache implements InjectionServerChunkCache {
}
