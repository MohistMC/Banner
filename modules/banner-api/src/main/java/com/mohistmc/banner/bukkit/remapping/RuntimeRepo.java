package com.mohistmc.banner.bukkit.remapping;

import net.md_5.specialsource.repo.ClassRepo;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RuntimeRepo
 *
 * @author Mainly by IzzelAliz
 * @originalClassName RuntimeRepo
 */
public class RuntimeRepo implements ClassRepo {

    private final Map<String, ClassNode> map = new ConcurrentHashMap<>();

    @Override
    public ClassNode findClass(String internalName) {
        return map.get(internalName);
    }

    public void put(byte[] bytes) {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, ClassReader.SKIP_CODE);
        this.map.put(reader.getClassName(), node);
    }
}
