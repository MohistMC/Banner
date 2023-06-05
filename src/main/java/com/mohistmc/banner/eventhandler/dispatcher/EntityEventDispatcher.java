package com.mohistmc.banner.eventhandler.dispatcher;

public class EntityEventDispatcher {

    public static void dispatchEntity() {
        /**
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {

            entity.damageEntity0(source, amount);
            return false;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            ItemStack heldStack = player.getItemInHand(hand);
            if (entity instanceof MushroomCow &&  heldStack.getItem() instanceof ShearsItem) {
                if (!CraftEventFactory.handlePlayerShearEntityEvent(player, entity, heldStack, hand)){
                    return InteractionResult.FAIL;
                }
                for (int i = 0; i < 5; ++i) {
                    ItemEntity entityitem = new ItemEntity(entity.level, entity.getX(), entity.getY(1.0D), entity.getZ(), new ItemStack(((MushroomCow) entity).getVariant().getBlockState().getBlock()));
                    EntityDropItemEvent event = new EntityDropItemEvent(entity.getBukkitEntity(), (org.bukkit.entity.Item) entityitem.getBukkitEntity());
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return InteractionResult.PASS;
                    }
                }
            }
            return InteractionResult.PASS;
        });*/
    }
}
