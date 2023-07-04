package com.mohistmc.banner.bukkit.pluginfix.fix;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class EssentialsXFix {

    public static byte[] transferItemDb(byte[] clazz) {
        ClassReader reader = new ClassReader(clazz);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode methodNode : node.methods) {
            if (methodNode.name.equals("getItemDbType") && methodNode.desc.equals("()Ljava/lang/String;")) {
                InsnList insnList = new InsnList();
                insnList.add(new LdcInsnNode("csv"));
                insnList.add(new InsnNode(Opcodes.ARETURN));
                methodNode.instructions = insnList;
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    public static byte[] fixMetrics(byte[] clzz) {
        ClassReader reader = new ClassReader(clzz);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        MethodNode targetMethod = null;

        for (MethodNode methodNode : node.methods) {
            if (methodNode.name.equals("<clinit>") && methodNode.desc.equals("()V")) {
                targetMethod = methodNode;
            }
        }

        if (targetMethod != null) {
            targetMethod.instructions.clear();
            targetMethod.instructions.insert(new InsnNode(Opcodes.RETURN));
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

}
