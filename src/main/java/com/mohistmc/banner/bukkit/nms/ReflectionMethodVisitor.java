package com.mohistmc.banner.bukkit.nms;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class ReflectionMethodVisitor extends MethodVisitor {

    public static ArrayList<String> SKIP = new ArrayList<>();
    static {
        SKIP.add("vault");
        SKIP.add("worldguard");
        SKIP.add("worldedit");
    }
    private String pln;
    private MappingResolver mr;
    private MappingResolver mr2;

    public static HashMap<String,String> spigot2obf;
    public static HashMap<String,String> cbm;

    public ReflectionMethodVisitor(int api, MethodVisitor visitMethod, String pln) {
        super(api, visitMethod);
        this.pln = pln;
        this.mr = FabricLoader.getInstance().getMappingResolver();
        this.mr2 = new Testing("official");
        if (null == spigot2obf) {
            spigot2obf = new HashMap<>();
            cbm = new HashMap<>();
            try {
                if (new File("builddata.txt").isFile()) {
                    for (String s : Files.readAllLines(new File("builddata.txt").toPath())) {
                        if (s.indexOf('#') != -1) continue;

                        String[] spl = s.split(" ");
                        spigot2obf.put(spl[1], spl[0]);
                        //   System.out.println("MAP: " + spl[1] + " | " + spl[0]);
                    }
                    System.out.println("Loaded 1.17 Obf Class Map: " + spigot2obf.size());
                    for (String s : Files.readAllLines(new File("bd-m.txt").toPath())) {
                        if (s.indexOf('#') != -1) continue;

                        String[] spl = s.split(" ");
                        spigot2obf.put(spl[0] + "#" + spl[3], spl[1]);
                    }
                    for (String s : Files.readAllLines(new File("cbm.txt").toPath())) {
                        if (s.indexOf('!') != -1) continue;

                        String[] spl = s.split("=");
                        System.out.println("SPLIT: " + spl[0] + "," + spl[1]);
                        cbm.put(spl[0].trim(), spl[1].trim());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int fixed = 0; // max so far: 484


    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (owner.startsWith("net/minecraft") && spigot2obf.size() > 1) {
            String own = spigot2obf.getOrDefault(owner, owner);
            String cl = mr.mapClassName("official", own.replace('/','.'));
            String d = desc;
            String d2 = desc;
            for (String s : spigot2obf.keySet()) {
                d = d.replace("L" + s + ";", "L" + spigot2obf.getOrDefault(s, s) + ";");
                d2 = d2.replace("L" + s + ";", "L" + mr.mapClassName("official", spigot2obf.getOrDefault(s, s).replace('/','.')) + ";").replace('.', '/');
            }

            if (!own.contains("v1_1")) {
                String name2 = mr.mapMethodName("official", own.replace('/', '.'),
                        spigot2obf.getOrDefault(owner + "#" + name, name).replace('/', '.'), d);

                if (!own.contains("net.minecraft.server.v1_1") && !name2.equals(name)) {
                    fixed++;
                } else if (!own.contains("net.minecraft.server.v1_1") && !name.contains("<init>")) {
                    String key = cl.substring(cl.lastIndexOf('.')+1) + "#" + name + d2;
                    name2 = cbm.getOrDefault(key, name2);
                    if (!cbm.containsKey(key) && name.length() < 3) {
                        try {
                            Class<?> cz = Class.forName(cl);
                            for (Method m : cz.getDeclaredMethods()) {
                                String tt = "";
                                for (Class<?> zz : m.getParameterTypes()) {
                                    tt += (fixName(zz.getName()) + ";");
                                }
                                tt = tt.replace("int;","I");
                                tt = "(" + tt + ")" + fixName(m.getReturnType().getName() + ";");
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }

                super.visitMethodInsn( opcode, cl.replace('.', '/'), name2, d2.replace('.', '/'), false );
                return;
            }
        }
        for (String str : SKIP) {
            if (this.pln.equalsIgnoreCase(str) || owner.startsWith("org/bukkit")) {
                // Skip Vault cause weird things happen
                super.visitMethodInsn( opcode, owner, name, desc, itf );
                return;
            }
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("forName") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/Class;"))
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "com/mohistmc/banner/bukkit/nms/ReflectionRemapper", "mapClassName", "(Ljava/lang/String;)Ljava/lang/String;", false);

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getMethods")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/mohistmc/banner/bukkit/nms/ReflectionRemapper", "getMethods", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", false );
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getField") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/reflect/Field;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/mohistmc/banner/bukkit/nms/ReflectionRemapper", "getFieldByName", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false );
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getDeclaredField") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/reflect/Field;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/mohistmc/banner/bukkit/nms/ReflectionRemapper", "getDeclaredFieldByName", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false );
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getMethod") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/reflect/Method;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/mohistmc/banner/bukkit/nms/ReflectionRemapper", "getMethodByName", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Method;", false );
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getDeclaredMethod") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/reflect/Method;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/mohistmc/banner/bukkit/nms/ReflectionRemapper", "getDeclaredMethodByName", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Method;", false );
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Package") && name.equalsIgnoreCase("getName") && desc.equalsIgnoreCase("()Ljava/lang/String;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/mohistmc/banner/bukkit/nms/ReflectionRemapper", "getPackageName", "(Ljava/lang/Package;)Ljava/lang/String;", false);
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getName") && desc.equalsIgnoreCase("()Ljava/lang/String;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/mohistmc/banner/bukkit/nms/ReflectionRemapper", "getClassName", "(Ljava/lang/Class;)Ljava/lang/String;", false);
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getCanonicalName") && desc.equalsIgnoreCase("()Ljava/lang/String;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/mohistmc/banner/bukkit/nms/ReflectionRemapper", "getCanonicalName", "(Ljava/lang/Class;)Ljava/lang/String;", false);
            return;
        }
        super.visitMethodInsn( opcode, owner, name, desc, itf );
    }

    private String fixName(String name) {
        String r = name.replace("boolean;", "Z").replace("byte;", "B").replace("double;", "D").replace("float;", "F").replace("int;", "I")
                .replace("long;", "J").replace("short;", "S").replace('.','/').replace("Lvoid","");

        if (r.length() > 3)
            r = "L" + r;
        return r;
    }

}
