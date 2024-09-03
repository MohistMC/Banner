package com.mohistmc.banner.injection.network.chat;

import net.minecraft.network.chat.Component;

import java.util.stream.Stream;

public interface InjectionComponent {

    default Stream<Component> bridge$stream() {
        throw new IllegalStateException("Not implemented");
    }
}
