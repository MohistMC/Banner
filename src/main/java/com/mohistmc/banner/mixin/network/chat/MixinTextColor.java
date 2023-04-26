package com.mohistmc.banner.mixin.network.chat;

import com.mohistmc.banner.injection.network.chat.InjectionTextColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TextColor.class)
public class MixinTextColor implements InjectionTextColor {

    @Nullable
    public ChatFormatting format;

    @Override
    public ChatFormatting bridge$format() {
        return format;
    }

    @Override
    public void banner$setFormat(ChatFormatting format) {
        this.format = format;
    }
}
