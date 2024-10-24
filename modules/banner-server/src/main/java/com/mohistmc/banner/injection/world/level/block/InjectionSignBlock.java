package com.mohistmc.banner.injection.world.level.block;

import io.papermc.paper.event.player.PlayerOpenSignEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public interface InjectionSignBlock {

    default void pushOpenSignCause(PlayerOpenSignEvent.Cause cause) {

    }

    default void openTextEdit(Player player, SignBlockEntity signEntity, boolean isFrontText, PlayerOpenSignEvent.Cause cause) {

    }
}
