package com.mohistmc.banner.injection.server.players;

import java.util.Collection;
import net.minecraft.server.players.StoredUserEntry;

public interface InjectionStoredUserList<K, V extends StoredUserEntry<K>> {


   default Collection<V> getValues() {
       throw new IllegalStateException("Not implemented");
   }
}
