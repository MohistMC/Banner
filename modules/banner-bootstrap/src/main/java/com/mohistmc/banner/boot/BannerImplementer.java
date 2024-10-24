package com.mohistmc.banner.boot;

import com.mohistmc.banner.asm.EnumDefinalizer;
import com.mohistmc.banner.asm.Implementer;
import com.mohistmc.banner.asm.InventoryImplementer;
import com.mohistmc.banner.asm.SwitchTableFixer;
import net.fabricmc.loader.impl.game.patch.GameTransformer;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BannerImplementer extends GameTransformer {

    private final GameTransformer delegate;
    private final MethodHandle getRawClassByteArray;

    private final Map<String, Implementer> implementers = new HashMap<>();

    public BannerImplementer(GameTransformer delegate, FabricLauncher launcher) throws Exception {
        this.delegate = delegate;
        Field field = launcher.getClass().getDeclaredField("classLoader");
        field.setAccessible(true);
        Object knotCl = field.get(launcher);
        Method method = knotCl.getClass().getDeclaredMethod("getRawClassByteArray", String.class, boolean.class);
        method.setAccessible(true);
        this.getRawClassByteArray = MethodHandles.lookup().unreflect(method).bindTo(knotCl);
        this.implementers.put("inventory", new InventoryImplementer());
        this.implementers.put("switch", SwitchTableFixer.INSTANCE);
        this.implementers.put("enum", new EnumDefinalizer());
    }

    @Override
    public void locateEntrypoints(FabricLauncher launcher, List<Path> gameJars) {
        delegate.locateEntrypoints(launcher, gameJars);
    }

    @Override
    public byte[] transform(String className) {
        byte[] bytes = delegate.transform(className);
        if (bytes == null) {
            try {
                bytes = (byte[]) this.getRawClassByteArray.invokeExact(className, false);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        if (bytes != null) {
            var reader = new ClassReader(bytes);
            var node = new ClassNode();
            reader.accept(node, 0);
            for (var entry : implementers.entrySet()) {
                var implementer = entry.getValue();
                implementer.processClass(node);
            }
            var cw = new ClassWriter(0);
            node.accept(cw);
            bytes = cw.toByteArray();
        }
        return bytes;
    }
}

