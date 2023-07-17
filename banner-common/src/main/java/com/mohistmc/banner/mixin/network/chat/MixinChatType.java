package com.mohistmc.banner.mixin.network.chat;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatType.class)
public abstract class MixinChatType {

    @Shadow
    private static ResourceKey<ChatType> create(String key) {
        return null;
    }

    private static final ResourceKey<ChatType> RAW = create("raw"); // CraftBukkit

    @Inject(method = "bootstrap", at = @At("RETURN"))
    private static void banner$bootstrapChat(BootstapContext<ChatType> context, CallbackInfo ci) {
        context.register(RAW, new ChatType(new ChatTypeDecoration("%s", List.of(ChatTypeDecoration.Parameter.CONTENT), Style.EMPTY), new ChatTypeDecoration("%s", List.of(ChatTypeDecoration.Parameter.CONTENT), Style.EMPTY))); // CraftBukkit
    }
}
