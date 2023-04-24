package com.mohistmc.banner.mixin.server.players;

import com.mohistmc.banner.injection.server.players.InjectionPlayerList;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;

// TODO fix inject method
@Mixin(PlayerList.class)
public class MixinPlayerList implements InjectionPlayerList {
}
