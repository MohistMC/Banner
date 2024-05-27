package com.mohistmc.banner.mixin.core.network.protocol.game;

import com.mohistmc.banner.asm.annotation.CreateConstructor;
import com.mohistmc.banner.asm.annotation.ShadowConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Banner TODO fix patches
@Mixin(ClientboundSystemChatPacket.class)
public class MixinClientboundSystemChatPacket {

    /*
    @ShadowConstructor
    public void banner$constructor(Component content, boolean overlay) {
        throw new RuntimeException();
    }

    @CreateConstructor
    public void banner$constructor(BaseComponent[] content, boolean overlay) {
        banner$constructor(Component.Serializer.fromJson(ComponentSerializer.toString(content)));
    }

    @Inject(method = "<init>(Lnet/minecraft/network/chat/Component;Z)V", at = @At("RETURN"))
    private void banner$init(Component content, boolean overlay, CallbackInfo ci) {
        this.a = Component.Serializer.toJson(content);
    }*/

    private String a;

    public String content0() {
        return a;
    }

}
