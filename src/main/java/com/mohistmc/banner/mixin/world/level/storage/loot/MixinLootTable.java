package com.mohistmc.banner.mixin.world.level.storage.loot;

import com.mohistmc.banner.injection.world.level.storage.loot.InjectionLootTable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.event.world.LootGenerateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(LootTable.class)
public abstract class MixinLootTable implements InjectionLootTable {

    @Shadow public abstract void fill(Container container, LootParams lootParams, long l);

    private AtomicBoolean banner$pluginUsed = new AtomicBoolean(false);

    @Override
    public void fillInventory(Container iinventory, LootParams lootparams, long i, boolean plugin) {
        banner$pluginUsed.set(plugin);
        fill(iinventory, lootparams, i);
    }

    @Inject(method = "fill", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/loot/LootTable;getAvailableSlots(Lnet/minecraft/world/Container;Lnet/minecraft/util/RandomSource;)Ljava/util/List;"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$fill(Container container, LootParams lootParams, long l, CallbackInfo ci,
                             LootContext lootContext, ObjectArrayList<ItemStack> objectArrayList,
                             RandomSource randomSource) {
        // CraftBukkit start
        LootGenerateEvent event = CraftEventFactory.callLootGenerateEvent(container, ((LootTable) (Object) this), lootContext, objectArrayList, banner$pluginUsed.get());
        if (event.isCancelled()) {
            ci.cancel();
        }
        objectArrayList = event.getLoot().stream().map(CraftItemStack::asNMSCopy).collect(ObjectArrayList.toList());
    }
    // CraftBukkit end
}
