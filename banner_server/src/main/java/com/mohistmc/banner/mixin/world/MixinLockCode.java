package com.mohistmc.banner.mixin.world;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.LockCode;
import net.minecraft.world.item.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LockCode.class)
public abstract class MixinLockCode {

    @Shadow @Final public String key;

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public boolean unlocksWith(ItemStack stack) {
        // CraftBukkit start - SPIGOT-6307: Check for color codes if the lock contains color codes
        if (this.key.isEmpty()) return true;
        if (!stack.isEmpty() && stack.get(DataComponents.CUSTOM_NAME) != null) {
            if (this.key.indexOf(ChatColor.COLOR_CHAR) == -1) {
                // The lock key contains no color codes, so let's ignore colors in the item display name (vanilla Minecraft behavior):
                return this.key.equals(stack.getHoverName().getString());
            } else {
                // The lock key contains color codes, so let's take them into account:
                return this.key.equals(CraftChatMessage.fromComponent(stack.getHoverName()));
            }
        }
        return false;
        // CraftBukkit end
    }
}
