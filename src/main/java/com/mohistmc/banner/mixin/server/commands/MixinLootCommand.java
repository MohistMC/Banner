package com.mohistmc.banner.mixin.server.commands;

import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootCommand.class)
public class MixinLootCommand {

    @Inject(method = "dropInWorld",
            at = @At(value = "INVOKE",
            target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V",
                    shift = At.Shift.BEFORE))
    private static void banner$ifRemove(CommandSourceStack source, Vec3 pos, List<ItemStack> items, LootCommand.Callback callback, CallbackInfoReturnable<Integer> cir) {
        items.removeIf(ItemStack::isEmpty); // CraftBukkit - SPIGOT-6959 Remove empty items for avoid throw an error in new EntityItem
    }
}
