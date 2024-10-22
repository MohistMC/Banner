package com.mohistmc.banner.mixin.world.level.levelgen.structure.templatesystem;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructurePlaceSettings.class)
public abstract class MixinStructurePlaceSettings {

    @Shadow public int palette;

    @Shadow public abstract RandomSource getRandom(@Nullable BlockPos seedPos);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void banner$resetValue(CallbackInfo ci) {
        this.palette = -1; // CraftBukkit - Set initial value so we know if the palette has been set forcefully
    }

    public StructureTemplate.Palette getRandomPalette(List<StructureTemplate.Palette> palettes, @Nullable BlockPos pos) {
        int i = palettes.size();
        if (i == 0) {
            throw new IllegalStateException("No palettes");
            // CraftBukkit start
        } else if (this.palette > 0) {
            if (this.palette >= i) {
                throw new IllegalArgumentException("Palette index out of bounds. Got " + this.palette + " where there are only " + i + " palettes available.");
            }
            return palettes.get(this.palette);
            // CraftBukkit end
        } else {
            return (StructureTemplate.Palette)palettes.get(this.getRandom(pos).nextInt(i));
        }
    }
}
