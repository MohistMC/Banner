package com.mohistmc.banner.bukkit.pluginfix.plugins;

import com.mohistmc.banner.bukkit.pluginfix.IPluginFixer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class CitizensFixer implements IPluginFixer {

    public static byte[] fixEffectsDirty(byte[] basicClass) {
        ClassReader classReader = new ClassReader(basicClass);
        ClassNode classNode = new ClassNode();
        ClassWriter classWriter = new ClassWriter(0);
        classReader.accept(classNode, 0);

        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.name.equals("bob") || fieldNode.name.equals("bV")) {
                fieldNode.name = "field_6285";
            }
        }

        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    @Override
    public byte[] injectPluginFix(String className, byte[] clazz) {
        if (className.equals("net.citizensnpcs.nms.v1_20_R1.entity.EntityHumanNPC")) {
            return fixEffectsDirty(clazz);
        }
        return clazz;
    }
}
