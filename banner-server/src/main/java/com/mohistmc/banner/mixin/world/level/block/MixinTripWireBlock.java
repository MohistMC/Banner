package com.mohistmc.banner.mixin.world.level.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TripWireBlock.class)
public abstract class MixinTripWireBlock extends Block {

    @Shadow @Final public static BooleanProperty POWERED;

    @Shadow protected abstract void updateSource(Level level, BlockPos pos, BlockState state);

    public MixinTripWireBlock(Properties properties) {
        super(properties);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    private void checkPressed(Level worldIn, BlockPos pos) {
        BlockState blockstate = worldIn.getBlockState(pos);
        boolean flag = blockstate.getValue(POWERED);
        boolean flag1 = false;
        List<? extends Entity> list = worldIn.getEntities(null, blockstate.getShape(worldIn, pos).bounds().move(pos));
        if (!list.isEmpty()) {
            for (Entity entity : list) {
                if (!entity.isIgnoringBlockTriggers()) {
                    flag1 = true;
                    break;
                }
            }
        }

        if (flag != flag1 && flag1 && blockstate.getValue(TripWireBlock.ATTACHED)) {
            org.bukkit.block.Block block = CraftBlock.at(worldIn, pos);
            boolean allowed = false;

            // If all of the events are cancelled block the tripwire trigger, else allow
            for (Object object : list) {
                if (object != null) {
                    Cancellable cancellable;

                    if (object instanceof Player) {
                        cancellable = CraftEventFactory.callPlayerInteractEvent((Player) object, Action.PHYSICAL, pos, null, null, null);
                    } else if (object instanceof Entity) {
                        cancellable = new EntityInteractEvent(((Entity) object).getBukkitEntity(), block);
                        Bukkit.getPluginManager().callEvent((EntityInteractEvent) cancellable);
                    } else {
                        continue;
                    }

                    if (!cancellable.isCancelled()) {
                        allowed = true;
                        break;
                    }
                }
            }

            if (!allowed) {
                return;
            }
        }

        if (flag1 != flag) {
            blockstate = blockstate.setValue(POWERED, flag1);
            worldIn.setBlock(pos, blockstate, 3);
            this.updateSource(worldIn, pos, blockstate);
        }

        if (flag1) {
            worldIn.scheduleTick(new BlockPos(pos), (Block) (Object) this, 10);
        }

    }
}
