package com.mohistmc.banner.mixin.core.network.protocol.game;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientboundSystemChatPacket.class)
public class MixinClientboundSystemChatPacket {

    public void banner$constructor(Component content, boolean overlay) {
        throw new RuntimeException();
    }

    public void banner$constructor(String content, boolean overlay) {
        banner$constructor(Component.Serializer.fromJson(content), overlay);
    }

    public void banner$constructor(BaseComponent[] content, boolean overlay) {
        banner$constructor(ComponentSerializer.toString(content), overlay);
    }
}
