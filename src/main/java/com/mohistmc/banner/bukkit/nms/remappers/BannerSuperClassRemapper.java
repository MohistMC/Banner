package com.mohistmc.banner.bukkit.nms.remappers;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.mohistmc.banner.bukkit.nms.proxy.DelegateClassLoder;
import com.mohistmc.banner.bukkit.nms.proxy.DelegateURLClassLoder;
import com.mohistmc.banner.bukkit.nms.utils.ASMUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class BannerSuperClassRemapper {
    public static Map<String, Class<?>> defineClass = Maps.newHashMap();

    public static void init(ClassNode node) {

        boolean remapSpClass  = false;
        switch (node.superName) {
            case ASMUtils.urlclassLoaderdesc -> {
                node.superName = Type.getInternalName(DelegateURLClassLoder.class);
                remapSpClass = true;
            }
            case ASMUtils.classLoaderdesc -> {
                defineClass.put(node.name + ";defineClass", DelegateClassLoder.class);
                node.superName = Type.getInternalName(DelegateClassLoder.class);
                remapSpClass = true;
            }
        }
        // https://github.com/Maxqia/ReflectionRemapper/blob/a75046eb0a864ad1f20b8f723ed467db614fff98/src/main/java/com/maxqia/ReflectionRemapper/Transformer.java#L68
        for (MethodNode method : node.methods) { // Taken from SpecialSource
            for (AbstractInsnNode next : method.instructions) {
                if (next instanceof TypeInsnNode insn && next.getOpcode() == Opcodes.NEW) { // remap new URLClassLoader
                    remapSpClass = switch (insn.desc) {
                        case ASMUtils.urlclassLoaderdesc -> {
                            insn.desc = Type.getInternalName(DelegateURLClassLoder.class);
                            yield true;
                        }
                        case ASMUtils.classLoaderdesc -> {
                            insn.desc = Type.getInternalName(DelegateClassLoder.class);
                            yield true;
                        }
                        default -> remapSpClass;
                    };
                }

                if (next instanceof MethodInsnNode ins) {
                    switch (ins.getOpcode()) {
                        case Opcodes.INVOKEVIRTUAL -> remapVirtual(next);
                        case Opcodes.INVOKESPECIAL -> {
                            if (remapSpClass && ins.name.equals("<init>")) {
                                switch (ins.owner) {
                                    case ASMUtils.urlclassLoaderdesc ->
                                            ins.owner = Type.getInternalName(DelegateURLClassLoder.class);
                                    case ASMUtils.classLoaderdesc ->
                                            ins.owner = Type.getInternalName(DelegateClassLoder.class);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // https://github.com/Maxqia/ReflectionRemapper/blob/a75046eb0a864ad1f20b8f723ed467db614fff98/src/main/java/com/maxqia/ReflectionRemapper/Transformer.java#L95
    public static void remapVirtual(AbstractInsnNode insn) {
        MethodInsnNode method = (MethodInsnNode) insn;
        Class<?> proxyClass = ReflectMethodRemapper.getVirtualMethod().get((method.owner + ";" + method.name));
        if (proxyClass != null) {
            Type returnType = Type.getReturnType(method.desc);
            ArrayList<Type> args = new ArrayList<>();
            args.add(Type.getObjectType(method.owner));
            args.addAll(Arrays.asList(Type.getArgumentTypes(method.desc)));

            method.setOpcode(Opcodes.INVOKESTATIC);
            method.owner = Type.getInternalName(proxyClass);
            method.desc = Type.getMethodDescriptor(returnType, args.toArray(new Type[0]));
        } else {
            proxyClass = defineClass.get((method.owner + ";" + method.name));
            if (proxyClass != null) {
                method.name += "Banner";
                method.owner = Type.getInternalName(proxyClass);
            }
        }
    }
}