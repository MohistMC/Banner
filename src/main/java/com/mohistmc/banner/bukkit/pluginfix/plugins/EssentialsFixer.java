package com.mohistmc.banner.bukkit.pluginfix.plugins;

import com.mohistmc.banner.bukkit.pluginfix.IPluginFixer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class EssentialsFixer implements IPluginFixer {

    @Override
    public byte[] injectPluginFix(String className, byte[] clazz) {
        if (className.equals("com.earth2me.essentials.utils.VersionUtil")) {
            return helloWorld(clazz, "net.fabricmc.loader.launch.knot.KnotServer", "hello.World");
        }
        if (className.equals("net.ess3.nms.refl.providers.ReflServerStateProvider")) {
            return helloWorld(clazz, "u", "U");
        }
        return clazz;
    }

    public static byte[] helloWorld(byte[] basicClass, String a, String b) {
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
