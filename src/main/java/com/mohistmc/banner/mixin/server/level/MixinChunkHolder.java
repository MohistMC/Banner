package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.injection.server.level.InjectionChunkHolder;
import net.minecraft.server.level.ChunkHolder;
import org.spongepowered.asm.mixin.Mixin;

// TODO fix inject method
@Mixin(ChunkHolder.class)
public class MixinChunkHolder implements InjectionChunkHolder {
}
