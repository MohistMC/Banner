package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.entity.monster.Silverfish$SilverfishWakeUpFriendsGoal")
public abstract class MixinSilverfish_WakeUpFriendsGoal extends Goal {

    @Shadow private int lookForFriends;

    @Shadow @Final private Silverfish silverfish;

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void tick() {
        --this.lookForFriends;
        if (this.lookForFriends <= 0) {
            Level level = this.silverfish.level();
            RandomSource randomSource = this.silverfish.getRandom();
            BlockPos blockPos = this.silverfish.blockPosition();

            for(int i = 0; i <= 5 && i >= -5; i = (i <= 0 ? 1 : 0) - i) {
                for(int j = 0; j <= 10 && j >= -10; j = (j <= 0 ? 1 : 0) - j) {
                    for(int k = 0; k <= 10 && k >= -10; k = (k <= 0 ? 1 : 0) - k) {
                        BlockPos blockPos2 = blockPos.offset(j, i, k);
                        BlockState blockState = level.getBlockState(blockPos2);
                        Block block = blockState.getBlock();
                        if (block instanceof InfestedBlock) {
                            // CraftBukkit start
                            if (!CraftEventFactory.callEntityChangeBlockEvent(this.silverfish, blockPos2, Blocks.AIR.defaultBlockState())) {
                                continue;
                            }
                            if (level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                                level.destroyBlock(blockPos2, true, this.silverfish);
                            } else {
                                level.setBlock(blockPos2, ((InfestedBlock)block).hostStateByInfested(level.getBlockState(blockPos2)), 3);
                            }

                            if (randomSource.nextBoolean()) {
                                return;
                            }
                        }
                    }
                }
            }
        }

    }
}
