package com.mohistmc.banner.injection.network.chat;

import net.minecraft.ChatFormatting;

public interface InjectionTextColor {

    default ChatFormatting bridge$format() {
        return null;
    }

    default void banner$setFormat(ChatFormatting format) {
    }
}
