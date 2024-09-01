package com.mohistmc.banner.mixin.world.level.block;

import com.mohistmc.banner.injection.world.level.block.InjectionSculkSpreader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkSpreader;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.SculkBloomEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkSpreader.class)
public abstract class MixinSculkSpreader implements InjectionSculkSpreader {

    @Shadow public abstract boolean isWorldGeneration();

    private transient Level banner$level;

    @Override
    public void banner$setLevel(Level level) {
        this.banner$level = level;
    }

    @Inject(method = "addCursor", cancellable = true, at = @At(value = "INVOKE", remap = false, target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private void banner$bloomEvent(SculkSpreader.ChargeCursor cursor, CallbackInfo ci) {
        if (!isWorldGeneration() && banner$level != null) {
            var bukkitBlock = CraftBlock.at(banner$level, cursor.pos);
            var event = new SculkBloomEvent(bukkitBlock, cursor.getCharge());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
            cursor.charge = event.getCharge();
        }
    }
}