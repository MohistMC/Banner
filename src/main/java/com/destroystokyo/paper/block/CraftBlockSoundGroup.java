package com.destroystokyo.paper.block;

import net.minecraft.world.level.block.SoundType;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_20_R1.CraftSound;

@Deprecated(forRemoval = true)
public class CraftBlockSoundGroup implements BlockSoundGroup {
    private final SoundType soundEffectType;

    public CraftBlockSoundGroup(SoundType soundEffectType) {
        this.soundEffectType = soundEffectType;
    }

    @Override
    public Sound getBreakSound() {
        return CraftSound.getBukkit(soundEffectType.getBreakSound());
    }

    @Override
    public Sound getStepSound() {
        return CraftSound.getBukkit(soundEffectType.getStepSound());
    }

    @Override
    public Sound getPlaceSound() {
        return CraftSound.getBukkit(soundEffectType.getPlaceSound());
    }

    @Override
    public Sound getHitSound() {
        return CraftSound.getBukkit(soundEffectType.getHitSound());
    }

    @Override
    public Sound getFallSound() {
        return CraftSound.getBukkit(soundEffectType.getFallSound());
    }
}
