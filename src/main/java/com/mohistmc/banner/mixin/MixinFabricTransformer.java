package com.mohistmc.banner.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.transformer.FabricTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FabricTransformer.class)
public class MixinFabricTransformer {

    @Inject(method = "transform",
            at = @At(value = "INVOKE",
            target = "Lorg/objectweb/asm/ClassReader;accept(Lorg/objectweb/asm/ClassVisitor;I)V",
                    ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private static void banner$fixWarning(boolean isDevelopment, EnvType envType, String name,
                                          byte[] bytes, CallbackInfoReturnable<byte[]> cir, boolean isMinecraftClass,
                                          boolean transformAccess, boolean environmentStrip, boolean applyAccessWidener,
                                          ClassReader classReader, ClassWriter classWriter, ClassVisitor visitor) {
        ClassNode node = new ClassNode();
        classReader.accept(node, 0);

        if (name.contains("CardboardWarning")) {
            cir.setReturnValue(helloWorld(bytes, "banner", "helloWolrd"));
        }
        node.accept(classWriter);
    }

    private static byte[] helloWorld(byte[] basicClass, String a, String b) {
        ClassReader classReader = new ClassReader(basicClass);
        ClassNode classNode = new ClassNode();
        ClassWriter classWriter = new ClassWriter(0);
        classReader.accept(classNode, 0);

        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode next : method.instructions) {
                if (next instanceof LdcInsnNode ldcInsnNode) {
                    if (ldcInsnNode.cst instanceof String str) {
                        if (a.equals(str)) {
                            ldcInsnNode.cst = b;
                        }
                    }
                }
            }
        }

        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
