package com.mohistmc.banner.mixin.network.protocol.game;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(method = "<init>(Lnet/minecraft/network/chat/Component;Z)V", at = @At("RETURN"))
    private void arclight$init(Component content, boolean overlay, CallbackInfo ci) {
        this.a = Component.Serializer.toJson(content);
    }

    private String a;


    /**
     * @author
     * @reason
     */
    @Overwrite
    public Component content() {
        return Component.Serializer.fromJson(a);
    }

    public String content0() {
        return a;
    }

}
