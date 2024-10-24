package com.mohistmc.banner.asm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface CreateConstructor {

    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.METHOD)
    @interface Merged {
    }
}
