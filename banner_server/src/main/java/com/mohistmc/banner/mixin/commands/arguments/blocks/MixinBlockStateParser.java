package com.mohistmc.banner.mixin.commands.arguments.blocks;

import com.mojang.brigadier.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockStateParser.class)
public class MixinBlockStateParser {

    @Mutable
    @Shadow @Final private Map<Property<?>, Comparable<?>> properties;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(HolderLookup holderLookup, StringReader stringReader, boolean bl, boolean bl2, CallbackInfo ci) {
        this.properties = new LinkedHashMap<>(properties);
    }
}
