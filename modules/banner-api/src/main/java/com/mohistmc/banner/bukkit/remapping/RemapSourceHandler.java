package com.mohistmc.banner.bukkit.remapping;

import com.google.common.io.ByteStreams;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.transformer.FabricTransformer;
import org.objectweb.asm.ClassReader;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Hashtable;

/**
 * RemapSourceHandler
 *
 * @author Mainly by IzzelAliz
 * @originalClassName RemapSourceHandler
 */

public class RemapSourceHandler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new RemapSourceConnection(new URL(u.getFile()));
    }

    private static class RemapSourceConnection extends URLConnection {

        private byte[] array;

        protected RemapSourceConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
            byte[] bytes = ByteStreams.toByteArray(url.openStream());
            String className = new ClassReader(bytes).getClassName();
            if (className.startsWith("net/minecraft/") || className.equals("com/mojang/brigadier/tree/CommandNode")) {
                bytes = fabricRemapClass(bytes);
            }
            this.array = Remapper.getResourceMapper().remapClassFile(bytes, GlobalClassRepo.INSTANCE);
        }

        public byte[] fabricRemapClass(byte[] cl) {
            var name = new ClassReader(cl).getClassName();
            var bytes = FabricTransformer.transform(false, EnvType.SERVER, name.replace('/', '.'), cl);
            bytes = ((IMixinTransformer) MixinEnvironment.getCurrentEnvironment().getActiveTransformer()).transformClassBytes(name, name, bytes);
            return bytes;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            connect();
            if (this.array == null) {
                throw new FileNotFoundException(this.url.getFile());
            } else {
                return new ByteArrayInputStream(this.array);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void register() {
        try {
            MethodHandles.lookup().ensureInitialized(URL.class);
            MethodHandle getter = Unsafe.lookup().findStaticGetter(URL.class, "handlers", Hashtable.class);
            Hashtable<String, URLStreamHandler> handlers = (Hashtable<String, URLStreamHandler>) getter.invokeExact();
            handlers.put("remap", new RemapSourceHandler());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
