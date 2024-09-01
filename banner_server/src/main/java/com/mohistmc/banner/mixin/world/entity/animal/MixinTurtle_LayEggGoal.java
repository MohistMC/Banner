package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.entity.animal.Turtle$TurtleLayEggGoal")
public abstract class MixinTurtle_LayEggGoal extends MoveToBlockGoal {

    @Shadow @Final private Turtle turtle;

    public MixinTurtle_LayEggGoal(PathfinderMob mob, double speedModifier, int searchRange) {
        super(mob, speedModifier, searchRange);
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void tick() {
        super.tick();
        BlockPos blockpos = this.turtle.blockPosition();
        if (!this.turtle.isInWater() && this.isReachedTarget()) {
            if (this.turtle.layEggCounter < 1) {
                this.turtle.setLayingEgg(true);
            } else if (this.turtle.layEggCounter > 200) {
                Level world = this.turtle.level();
                if (CraftEventFactory.callEntityChangeBlockEvent(this.turtle, this.blockPos.above(), (Blocks.TURTLE_EGG.defaultBlockState()).setValue(TurtleEggBlock.EGGS, this.turtle.getRandom().nextInt(4) + 1))) {
                    BlockPos blockpos1 = this.blockPos.above();
                    BlockState blockstate = Blocks.TURTLE_EGG.defaultBlockState().setValue(TurtleEggBlock.EGGS, this.turtle.getRandom().nextInt(4) + 1);
                    world.setBlock(blockpos1, blockstate, 3);
                    world.gameEvent(GameEvent.BLOCK_PLACE, blockpos1, GameEvent.Context.of(this.turtle, blockstate));
                }
                this.turtle.setHasEgg(false);
                this.turtle.setLayingEgg(false);
                this.turtle.setInLoveTime(600);
            }

            if (this.turtle.isLayingEgg()) {
                this.turtle.layEggCounter = this.turtle.layEggCounter + 1;
            }
        }

    }
}
