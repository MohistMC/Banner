package com.mohistmc.banner.bukkit.remapping;

import com.google.common.io.ByteStreams;
import io.izzel.tools.product.Product2;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.CodeSource;
import java.util.concurrent.Callable;
import java.util.jar.Manifest;

/**
 * RemappingURLClassLoader
 *
 * @author Mainly by IzzelAliz
 * @originalClassName ArclightReflectionHandler
 */
public class RemappingURLClassLoader extends URLClassLoader implements RemappingClassLoader {
    private final ClassLoader mainParent;

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public RemappingURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, RemappingClassLoader.asTransforming(parent));
        this.mainParent = new URLClassLoader(urls, parent);
    }

    public RemappingURLClassLoader(URL[] urls) {
        this(urls, RemappingClassLoader.asTransforming(null));
    }

    public RemappingURLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, RemappingClassLoader.asTransforming(parent), factory);
        this.mainParent = new URLClassLoader(urls, parent);
    }

    public RemappingURLClassLoader(String name, URL[] urls, ClassLoader parent) {
        super(name, urls, RemappingClassLoader.asTransforming(parent));
        this.mainParent = new URLClassLoader(urls, parent);
    }

    public RemappingURLClassLoader(String name, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(name, urls, RemappingClassLoader.asTransforming(parent), factory);
        this.mainParent = new URLClassLoader(urls, parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> result = null;
        String path = name.replace('.', '/').concat(".class");
        URL resource = this.findResource(path);
        if (resource != null) {
            URLConnection connection;
            Callable<byte[]> byteSource;
            Manifest manifest;
            try {
                connection = resource.openConnection();
                connection.connect();
                if (connection instanceof JarURLConnection && ((JarURLConnection) connection).getManifest() != null) {
                    manifest = ((JarURLConnection) connection).getManifest();
                } else {
                    manifest = null;
                }
                byteSource = () -> {
                    try (InputStream is = connection.getInputStream()) {
                        byte[] classBytes = ByteStreams.toByteArray(is);
                        return classBytes;
                    }
                };
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }

            Product2<byte[], CodeSource> classBytes = this.getRemapper().remapClass(name, byteSource, connection);

            int i = name.lastIndexOf('.');
            if (i != -1) {
                String pkgName = name.substring(0, i);
                if (getPackage(pkgName) == null) {
                    try {
                        if (manifest != null) {
                            definePackage(pkgName, manifest, resource);
                        } else {
                            definePackage(pkgName, null, null, null, null, null, null, null);
                        }
                    } catch (IllegalArgumentException ex) {
                        if (getPackage(pkgName) == null) {
                            throw new IllegalStateException("Cannot find package " + pkgName);
                        }
                    }
                }
            }

            try {
                result = this.defineClass(name, classBytes._1, 0, classBytes._1.length, classBytes._2);
            } catch (NoClassDefFoundError ignored) {
                result = mainParent.loadClass(name);
            }
        }
        if (result == null) {
            throw new ClassNotFoundException(name);
        }
        return result;
    }

    private ClassLoaderRemapper remapper;

    @Override
    public ClassLoaderRemapper getRemapper() {
        if (remapper == null) {
            remapper = Remapper.createClassLoaderRemapper(this);
        }
        return remapper;
    }
}
