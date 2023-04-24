package com.mohistmc.banner.injection.world.level;

public interface InjectionBlock {

    // Spigot start
    static float range(float min, float value, float max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
    // Spigot end
}
