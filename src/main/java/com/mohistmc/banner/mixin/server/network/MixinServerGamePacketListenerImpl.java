package com.mohistmc.banner.mixin.server.network;

import com.mohistmc.banner.injection.server.network.InjectionServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListenerImpl implements InjectionServerGamePacketListenerImpl {
}
