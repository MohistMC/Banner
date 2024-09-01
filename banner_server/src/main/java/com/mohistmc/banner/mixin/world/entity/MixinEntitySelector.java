package com.mohistmc.banner.mixin.world.entity;

import com.google.common.base.Predicates;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntitySelector.class)
public class MixinEntitySelector {

    @Shadow @Final public static Predicate<Entity> NO_SPECTATORS;

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public static Predicate<Entity> pushableBy(Entity entity) {
        Team team = entity.getTeam();
        Team.CollisionRule collisionRule = team == null ? Team.CollisionRule.ALWAYS : team.getCollisionRule();
        return (Predicate)(collisionRule == Team.CollisionRule.NEVER ? Predicates.alwaysFalse() : NO_SPECTATORS.and((entity2) -> {
            if (!entity2.canCollideWithBukkit(entity) || !entity.canCollideWithBukkit(entity2)) { // CraftBukkit - collidable API
                return false;
            } else if (entity.level().isClientSide && (!(entity2 instanceof Player) || !((Player)entity2).isLocalPlayer())) {
                return false;
            } else {
                Team team2 = entity2.getTeam();
                Team.CollisionRule collisionRule2 = team2 == null ? Team.CollisionRule.ALWAYS : team2.getCollisionRule();
                if (collisionRule2 == Team.CollisionRule.NEVER) {
                    return false;
                } else {
                    boolean bl = team != null && team.isAlliedTo(team2);
                    if ((collisionRule == Team.CollisionRule.PUSH_OWN_TEAM || collisionRule2 == Team.CollisionRule.PUSH_OWN_TEAM) && bl) {
                        return false;
                    } else {
                        return collisionRule != Team.CollisionRule.PUSH_OTHER_TEAMS && collisionRule2 != Team.CollisionRule.PUSH_OTHER_TEAMS || bl;
                    }
                }
            }
        }));
    }
}
