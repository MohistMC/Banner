package com.mohistmc.banner.bukkit.remapping;

import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;

/**
 * LenientJarRemapper
 *
 * @author Mainly by IzzelAliz
 * @originalClassName LenientJarRemapper
 */
public class LenientJarRemapper extends JarRemapper {

    public LenientJarRemapper(JarMapping jarMapping) {
        super(jarMapping);
    }

    @Override
    public String mapSignature(String signature, boolean typeSignature) {
        try {
            return super.mapSignature(signature, typeSignature);
        } catch (Exception e) {
            return signature;
        }
    }
}
