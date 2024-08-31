package com.mohistmc.banner.injection.network.chat;

import java.util.stream.Stream;
import net.minecraft.network.chat.Component;

public interface InjectionComponent {

    default Stream<Component> bridge$stream() {
        throw new IllegalStateException("Not implemented");
    }
}
