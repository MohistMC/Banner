package com.mohistmc.banner.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.reflect.Modifier;

public class InventoryImplementer implements Implementer {

    private static final String BRIDGE_TYPE = "com/mohistmc/banner/injection/world/InjectionContainer";

    private final String maxStackSizeMethodName;

    public InventoryImplementer() {
        this.maxStackSizeMethodName = "method_5444";
    }

    @Override
    public boolean processClass(ClassNode node) {
        if (Modifier.isInterface(node.access) || node.interfaces.contains(BRIDGE_TYPE)) {
            return false;
        }
        return tryImplement(node);
    }

    private boolean tryImplement(ClassNode node) {
        MethodNode stackLimitMethod = null;
        for (MethodNode method : node.methods) {
            if (!Modifier.isAbstract(method.access) && method.name.equals(this.maxStackSizeMethodName) && method.desc.equals("()I")) {
                stackLimitMethod = method;
                break;
            }
        }
        if (stackLimitMethod == null) {
            return false;
        } else {
            for (MethodNode method : node.methods) {
                if (method.name.equals("setMaxStackSize") && method.desc.equals("(I)V")) {
                    return false;
                }
            }

            FieldNode maxStack = new FieldNode(Opcodes.ACC_PRIVATE, "bannerF$maxStack", Type.getType(Integer.class).getDescriptor(), null, null);
            node.fields.add(maxStack);
            node.interfaces.add(BRIDGE_TYPE);
            InsnList list = new InsnList();
            LabelNode labelNode = new LabelNode();
            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
            list.add(new FieldInsnNode(Opcodes.GETFIELD, node.name, maxStack.name, maxStack.desc));
            list.add(new InsnNode(Opcodes.DUP));
            list.add(new JumpInsnNode(Opcodes.IFNULL, labelNode));
            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Integer.class), "intValue", "()I", false));
            list.add(new InsnNode(Opcodes.IRETURN));
            list.add(labelNode);
            list.add(new FrameNode(Opcodes.F_SAME1, 0,  null, 1, new Object[]{Type.getInternalName(Integer.class)}));
            list.add(new InsnNode(Opcodes.POP));
            stackLimitMethod.maxStack = Math.max(2, stackLimitMethod.maxStack);
            stackLimitMethod.instructions.insert(list);
            {
                MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, "setMaxStackSize", "(I)V", null, null);
                InsnList insnList = new InsnList();
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insnList.add(new VarInsnNode(Opcodes.ILOAD, 1));
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Integer.class), "valueOf", Type.getMethodDescriptor(Type.getType(Integer.class), Type.INT_TYPE)));
                insnList.add(new FieldInsnNode(Opcodes.PUTFIELD, node.name, maxStack.name, maxStack.desc));
                insnList.add(new InsnNode(Opcodes.RETURN));
                methodNode.maxLocals = 2;
                methodNode.maxStack = 2;
                methodNode.instructions = insnList;
                node.methods.add(methodNode);
            }
            return true;
        }
    }
}
