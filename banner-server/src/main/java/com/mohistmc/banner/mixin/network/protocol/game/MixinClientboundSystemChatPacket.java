package com.mohistmc.banner.mixin.network.protocol.game;

import com.mohistmc.banner.asm.annotation.CreateConstructor;
import com.mohistmc.banner.asm.annotation.ShadowConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientboundSystemChatPacket.class)
public class MixinClientboundSystemChatPacket {

    @ShadowConstructor
    public void banner$constructor(Component content, boolean overlay) {
        throw new RuntimeException();
    }

    @CreateConstructor
    public void banner$constructor(BaseComponent[] content, boolean overlay) {
        banner$constructor(CraftChatMessage.fromJSON(ComponentSerializer.toString(content)), overlay);
    }

    private String a;

    public String content0() {
        return a;
    }

}
