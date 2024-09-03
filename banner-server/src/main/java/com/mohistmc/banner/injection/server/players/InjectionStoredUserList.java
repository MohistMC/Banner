package com.mohistmc.banner.injection.server.players;

import net.minecraft.server.players.StoredUserEntry;

import java.util.Collection;

public interface InjectionStoredUserList<K, V extends StoredUserEntry<K>> {


   default Collection<V> getValues() {
       throw new IllegalStateException("Not implemented");
   }
}
