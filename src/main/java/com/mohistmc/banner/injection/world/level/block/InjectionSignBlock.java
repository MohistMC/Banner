package com.mohistmc.banner.injection.world.level.block;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.bukkit.event.player.PlayerSignOpenEvent;

public interface InjectionSignBlock {

    default void pushOpenSignCause(PlayerSignOpenEvent.Cause cause) {
        throw new IllegalStateException("Not implemented");
    }

    default void openTextEdit(Player player, SignBlockEntity signEntity, boolean isFrontText, PlayerSignOpenEvent.Cause cause) {
        throw new IllegalStateException("Not implemented");
    }
}
