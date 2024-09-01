package com.mohistmc.banner.mixin.world.level.storage.loot;

import com.mohistmc.banner.injection.world.level.storage.loot.InjectionLootTable;
import io.izzel.arclight.mixin.Eject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.bukkit.craftbukkit.CraftLootTable;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.world.LootGenerateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootTable.class)
public abstract class MixinLootTable implements InjectionLootTable {
    @Shadow protected abstract ObjectArrayList<ItemStack> getRandomItems(LootContext context);
    @Shadow public abstract void fill(Container container, LootParams params, long seed);

    public CraftLootTable craftLootTable; // CraftBukkit

    @Eject(method = "fill", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    private ObjectArrayList<ItemStack> banner$nonPluginEvent(LootTable lootTable, LootContext context, CallbackInfo ci, Container inv) {
        ObjectArrayList<ItemStack> list = this.getRandomItems(context);
        if (context.hasParam(LootContextParams.ORIGIN) && context.hasParam(LootContextParams.THIS_ENTITY)) {
            LootGenerateEvent event = CraftEventFactory.callLootGenerateEvent(inv, (LootTable) (Object) this, context, list, isPlugin.getAndSet(false));
            if (event != null) {
                if (event.isCancelled()) {
                    ci.cancel();
                    return null;
                }
                return event.getLoot().stream().map(CraftItemStack::asNMSCopy).collect(ObjectArrayList.toList());
            }
        }
        return list;
    }

    public AtomicBoolean isPlugin = new AtomicBoolean(false);

    @Override
    public void fillInventory(Container inv, LootParams lootparams, long seed, boolean plugin) {
        isPlugin.set(plugin);
        fill(inv, lootparams, seed);
    }

    @Override
    public CraftLootTable bridge$craftLootTable() {
        return craftLootTable;
    }

    @Override
    public void banner$setCraftLootTable(CraftLootTable craftLootTable) {
        this.craftLootTable = craftLootTable;
    }
}
