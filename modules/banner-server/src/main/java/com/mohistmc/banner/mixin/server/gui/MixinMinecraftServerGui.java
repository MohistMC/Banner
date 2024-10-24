package com.mohistmc.banner.mixin.server.gui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import net.minecraft.server.gui.MinecraftServerGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServerGui.class)
public class MixinMinecraftServerGui {

    private static final java.util.regex.Pattern ANSI = java.util.regex.Pattern.compile("\\x1B\\[([0-9]{1,2}(;[0-9]{1,2})*)?[m|K]"); // CraftBukkit

    @Redirect(method = "print", at = @At(value = "INVOKE", target = "Ljavax/swing/text/Document;insertString(ILjava/lang/String;Ljavax/swing/text/AttributeSet;)V"))
    private void banner$resetString(Document document, int i, String s, AttributeSet attributeSet) throws BadLocationException {
        document.insertString(document.getLength(), ANSI.matcher(s).replaceAll(""), (AttributeSet) null); // CraftBukkit
    }
}
