package com.mohistmc.banner.mixin.util.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.EntityCustomNameToComponentFix;
import net.minecraft.util.datafix.fixes.References;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DataFixers.class)
public class MixinDataFixers {

    @Inject(method = "addFixers",
            at = @At(value = "INVOKE",
            target = "Lcom/mojang/datafixers/DataFixerBuilder;addSchema(ILjava/util/function/BiFunction;)Lcom/mojang/datafixers/schemas/Schema;",
            remap = false,
            ordinal = 38), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void banner$addFix(DataFixerBuilder builder, CallbackInfo ci,
                                      Schema schema, Schema schema2, Schema schema3,
                                      Schema schema4, Schema schema5, Schema schema6,
                                      Schema schema7, Schema schema8, Schema schema9,
                                      Schema schema10, Schema schema11, Schema schema12,
                                      Schema schema13, Schema schema14, Schema schema15,
                                      Schema schema16, Schema schema17, Schema schema18,
                                      Schema schema19, Schema schema20, Schema schema21,
                                      Schema schema22, Schema schema23, Schema schema24,
                                      Schema schema25, Schema schema26, Schema schema27,
                                      Schema schema28, Schema schema29, Schema schema30,
                                      Schema schema31, Schema schema32, Schema schema33,
                                      Schema schema34, Schema schema35, Schema schema36,
                                      Schema schema37, Schema schema38, Schema schema39,
                                      Schema schema40, Schema schema41, Schema schema42,
                                      Schema schema43, Schema schema44, Schema schema45) {
        // CraftBukkit start
        builder.addFixer(new com.mojang.datafixers.DataFix(schema45, false) {
            @Override
            protected com.mojang.datafixers.TypeRewriteRule makeRule() {
                return this.fixTypeEverywhereTyped("Player CustomName", this.getInputSchema().getType(References.PLAYER), (typed) -> {
                    return typed.update(DSL.remainderFinder(), (dynamic) -> {
                        return EntityCustomNameToComponentFix.fixTagCustomName(dynamic);
                    });
                });
            }
        });
    }
}
