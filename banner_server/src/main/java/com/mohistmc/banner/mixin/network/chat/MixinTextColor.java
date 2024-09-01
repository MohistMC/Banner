package com.mohistmc.banner.mixin.network.chat;

import com.mohistmc.banner.asm.annotation.CreateConstructor;
import com.mohistmc.banner.asm.annotation.ShadowConstructor;
import com.mohistmc.banner.injection.network.chat.InjectionTextColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextColor.class)
public class MixinTextColor implements InjectionTextColor {

    // @formatter:off
    @Mutable @Shadow @Final @Nullable
    private String name;
    @Nullable public ChatFormatting format;
    // @formatter:on

    @Override
    public ChatFormatting bridge$format() {
        return format;
    }

    @ShadowConstructor
    public void banner$constructor(int color) {
        throw new RuntimeException();
    }

    @CreateConstructor
    public void banner$constructor(int color, String name, ChatFormatting textFormatting) {
        banner$constructor(color);
        this.name = name;
        this.format = textFormatting;
    }

    @Inject(method = "<init>(ILjava/lang/String;)V", at = @At("RETURN"))
    private void banner$withFormat(int color, String name, CallbackInfo ci) {
        this.format = ChatFormatting.getByName(name);
    }

    @Override
    public void banner$setFormat(ChatFormatting format) {
        this.format = format;
    }
}
