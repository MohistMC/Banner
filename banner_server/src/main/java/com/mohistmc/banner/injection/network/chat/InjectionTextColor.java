package com.mohistmc.banner.injection.network.chat;

import net.minecraft.ChatFormatting;

public interface InjectionTextColor {

    default ChatFormatting bridge$format() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setFormat(ChatFormatting format) {
        throw new IllegalStateException("Not implemented");
    }
}
