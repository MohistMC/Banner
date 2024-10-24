package com.mohistmc.banner.fabric;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;

public class FabricEventFactory {

    public static Event<HookBukkit> HOOK_BUKKIT = EventFactory.createArrayBacked(HookBukkit.class,
            (listeners) -> (bukkitEvent) -> {
                for (HookBukkit listener : listeners) {
                    listener.hook(bukkitEvent);
                }
            });

    public static final Event<AddEntity> ADD_ENTITY_EVENT = EventFactory.createArrayBacked(AddEntity.class,
            (listeners) -> (entity) -> {
                for (AddEntity event : listeners) {
                    boolean result = event.addFreshEntity(entity);

                    if (!result) {
                        return false;
                    }
                }

                return true;
            }
    );
    public static final Event<CanceledAddEntity> CANCELED_ADD_ENTITY_EVENT = EventFactory.createArrayBacked(CanceledAddEntity.class,
            (listeners) -> (entity) -> {
                for (CanceledAddEntity event : listeners) {
                    event.canceledAddFreshEntity(entity);
                }
            }
    );

    @FunctionalInterface
    public interface HookBukkit {
        void hook(org.bukkit.event.Event bukkitEvent);
    }

    @FunctionalInterface
    public interface AddEntity {
        boolean addFreshEntity(Entity entity);
    }

    @FunctionalInterface
    public interface CanceledAddEntity {
        void canceledAddFreshEntity(Entity entity);
    }
}
