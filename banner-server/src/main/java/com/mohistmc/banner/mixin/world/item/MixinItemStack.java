package com.mohistmc.banner.mixin.world.item;

import com.mohistmc.banner.bukkit.BukkitFieldHooks;
import com.mohistmc.banner.injection.world.item.InjectionItemStack;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements InjectionItemStack {

    // @formatter:off
    @Shadow @Deprecated private Item item;
    @Shadow private int count;
    // @formatter:on

    @Shadow public abstract Item getItem();

    @Shadow public abstract void setDamageValue(int damage);

    @Shadow public abstract int getDamageValue();

    @Shadow public abstract int getCount();

    @Shadow public abstract void setCount(int count);

    @Shadow public abstract ItemStack copy();

    @Shadow public abstract void shrink(int decrement);

    @Shadow @Final private PatchedDataComponentMap components;

    @Shadow public abstract void applyComponents(DataComponentMap dataComponentMap);

    @Shadow public abstract boolean isDamageableItem();

    @Shadow public abstract int getMaxDamage();

    @Shadow public abstract boolean canPlaceOnBlockInAdventureMode(BlockInWorld blockInWorld);

    @Shadow public abstract void applyComponents(DataComponentPatch dataComponentPatch);

    @Override
    public void restorePatch(DataComponentPatch empty) {
        this.components.restorePatch(empty);
    }

    /**
     * @author wdog5
     * @reason functionality replaced
     * TODO inline this with injects
     */
    @Overwrite
    public void hurtAndBreak(int i, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        Level var5 = livingEntity.level();
        if (var5 instanceof ServerLevel serverLevel) {
            ServerPlayer var10003;
            if (livingEntity instanceof ServerPlayer serverPlayer) {
                var10003 = serverPlayer;
            } else {
                var10003 = null;
            }

            this.hurtAndBreak(i, serverLevel, var10003, (item) -> {
                if (this.count == 1 && livingEntity instanceof Player) {
                    CraftEventFactory.callPlayerItemBreakEvent(((Player) livingEntity), (ItemStack) (Object) this);
                }
                livingEntity.onEquippedItemBroken(item, equipmentSlot);
            });
        }

    }

    @SuppressWarnings("all")
    @Override
    @Deprecated
    public void setItem(Item item) {
        this.item = item;
    }

    /**
     * @author wdog5
     * @reason functionality replaced
     * TODO inline this with injects
     */
    @Overwrite
    public void hurtAndBreak(int i, ServerLevel serverLevel, @Nullable ServerPlayer serverPlayer, Consumer<Item> consumer) {
        if (this.isDamageableItem()) {
            if (serverPlayer == null || !serverPlayer.hasInfiniteMaterials()) {
                if (i > 0) {
                    i = EnchantmentHelper.processDurabilityChange(serverLevel, ((ItemStack) (Object) this), i);
                    // CraftBukkit start
                    if (serverPlayer != null) {
                        PlayerItemDamageEvent event = new PlayerItemDamageEvent(serverPlayer.getBukkitEntity(), CraftItemStack.asCraftMirror(((ItemStack) (Object) this)), i);
                        event.getPlayer().getServer().getPluginManager().callEvent(event);

                        if (i != event.getDamage() || event.isCancelled()) {
                            event.getPlayer().updateInventory();
                        }
                        if (event.isCancelled()) {
                            return;
                        }

                        i = event.getDamage();
                    }
                    // CraftBukkit end
                    if (i <= 0) {
                        return;
                    }
                }

                if (serverPlayer != null && i != 0) {
                    CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(serverPlayer, ((ItemStack) (Object) this), this.getDamageValue() + i);
                }

                int j = this.getDamageValue() + i;
                this.setDamageValue(j);
                if (j >= this.getMaxDamage()) {
                    Item item = this.getItem();
                    this.shrink(1);
                    consumer.accept(item);
                }

            }
        }
    }

    /**
     * @author wdog5
     * @reason functionality replaced
     * TODO inline this with injects
     */
    @Overwrite
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        BlockPos blockPos = context.getClickedPos();
        BlockInWorld blockInWorld = new BlockInWorld(context.getLevel(), blockPos, false);
        if (player != null && !player.getAbilities().mayBuild && !this.canPlaceOnBlockInAdventureMode(new BlockInWorld(context.getLevel(), blockPos, false))) {
            return InteractionResult.PASS;
        } else {
            Item item = this.getItem();
            // CraftBukkit start - handle all block place event logic here
            PatchedDataComponentMap oldData = this.getComponentsClone();
            int oldCount = this.getCount();
            ServerLevel world = (ServerLevel) context.getLevel();
            if (!(item instanceof BucketItem || item instanceof SolidBucketItem)) { // if not bucket
                world.banner$setCaptureBlockStates(true);
                // special case bonemeal
                if (item == Items.BONE_MEAL) {
                    world.banner$setCaptureTreeGeneration(true);
                }
            }
            InteractionResult interactionResult;
            try {
                interactionResult = item.useOn(context);
            } finally {
                world.banner$setCaptureBlockStates(false);
            }
            PatchedDataComponentMap newData = this.getComponentsClone();
            int newCount = this.getCount();
            this.setCount(oldCount);
            this.setComponentsClone(oldData);
            if (interactionResult.consumesAction() && world.bridge$captureTreeGeneration() && !world.bridge$capturedBlockStates().isEmpty()) {
                world.banner$setCaptureTreeGeneration(false);
                Location location = CraftLocation.toBukkit(blockPos, world.getWorld());
                TreeType treeType = BukkitFieldHooks.treeType();
                BukkitFieldHooks.setTreeType(null);
                List<CraftBlockState> blocks = new java.util.ArrayList<>(world.bridge$capturedBlockStates().values());
                world.bridge$capturedBlockStates().clear();
                StructureGrowEvent structureEvent = null;
                if (treeType != null) {
                    boolean isBonemeal = getItem() == Items.BONE_MEAL;
                    structureEvent = new StructureGrowEvent(location, treeType, isBonemeal, (org.bukkit.entity.Player) player.getBukkitEntity(), (List< org.bukkit.block.BlockState>) (List<? extends org.bukkit.block.BlockState>) blocks);
                    org.bukkit.Bukkit.getPluginManager().callEvent(structureEvent);
                }

                BlockFertilizeEvent fertilizeEvent = new BlockFertilizeEvent(CraftBlock.at(world, blockPos), (org.bukkit.entity.Player) player.getBukkitEntity(), (List< org.bukkit.block.BlockState>) (List<? extends org.bukkit.block.BlockState>) blocks);
                fertilizeEvent.setCancelled(structureEvent != null && structureEvent.isCancelled());
                org.bukkit.Bukkit.getPluginManager().callEvent(fertilizeEvent);

                if (!fertilizeEvent.isCancelled()) {
                    // Change the stack to its new contents if it hasn't been tampered with.
                    if (this.getCount() == oldCount && Objects.equals(this.components, oldData)) {
                        this.applyComponents(newData);
                        this.setCount(newCount);
                    }
                    for (CraftBlockState blockstate : blocks) {
                        world.setBlock(blockstate.getPosition(),blockstate.getHandle(), blockstate.getFlag()); // SPIGOT-7248 - manual update to avoid physics where appropriate
                    }
                    player.awardStat(Stats.ITEM_USED.get(item)); // SPIGOT-7236 - award stat
                }
                BukkitFieldHooks.setOpenSign(null); // SPIGOT-6758 - Reset on early return // Banner - cancel
                return interactionResult;
            }
            world.banner$setCaptureTreeGeneration(false);
            if (player != null && interactionResult.indicateItemUse()) {
                InteractionHand bannerHand = context.getHand(); // Banner
                org.bukkit.event.block.BlockPlaceEvent placeEvent = null;
                List<org.bukkit.block.BlockState> blocks = new java.util.ArrayList<>(world.bridge$capturedBlockStates().values());
                world.bridge$capturedBlockStates().clear();
                if (blocks.size() > 1) {
                    placeEvent = CraftEventFactory.callBlockMultiPlaceEvent(world, player, bannerHand, blocks, blockPos.getX(), blockPos.getY(), blockPos.getZ());
                } else if (blocks.size() == 1) {
                    placeEvent = CraftEventFactory.callBlockPlaceEvent(world, player, bannerHand, blocks.get(0), blockPos.getX(), blockPos.getY(), blockPos.getZ());
                }

                if (placeEvent != null && (placeEvent.isCancelled() || !placeEvent.canBuild())) {
                    interactionResult = InteractionResult.FAIL; // cancel placement
                    // PAIL: Remove this when MC-99075 fixed
                    placeEvent.getPlayer().updateInventory();
                    // revert back all captured blocks
                    world.banner$setPreventPoiUpdated(true); // CraftBukkit - SPIGOT-5710
                    for (org.bukkit.block.BlockState blockstate : blocks) {
                        blockstate.update(true, false);
                    }
                    world.banner$setPreventPoiUpdated(false);

                    // Brute force all possible updates
                    BlockPos placedPos = ((CraftBlock) placeEvent.getBlock()).getPosition();
                    for (Direction dir : Direction.values()) {
                        ((ServerPlayer) player).connection.send(new ClientboundBlockUpdatePacket(world, placedPos.relative(dir)));
                    }
                    BukkitFieldHooks.setOpenSign(null); // SPIGOT-6758 - Reset on early return // Banner - cancel
                } else {
                    // Change the stack to its new contents if it hasn't been tampered with.
                    if (this.getCount() == oldCount && Objects.equals(this.components, oldData)) {
                        this.applyComponents(newData);
                        this.setCount(newCount);
                    }

                    for (Map.Entry<BlockPos, BlockEntity> e : world.bridge$capturedTileEntities().entrySet()) {
                        world.setBlockEntity(e.getValue());
                    }

                    for (org.bukkit.block.BlockState blockstate : blocks) {
                        int updateFlag = ((CraftBlockState) blockstate).getFlag();
                        BlockState oldBlock = ((CraftBlockState) blockstate).getHandle();
                        BlockPos newblockposition = ((CraftBlockState) blockstate).getPosition();
                        BlockState block = world.getBlockState(newblockposition);

                        if (!(block.getBlock() instanceof BaseEntityBlock)) { // Containers get placed automatically
                            block.getBlock().onPlace(block, world, newblockposition, oldBlock, true);
                        }

                        world.notifyAndUpdatePhysics(newblockposition, null, oldBlock, block, world.getBlockState(newblockposition), updateFlag, 512); // send null chunk as chunk.k() returns false by this point
                    }

                    if (this.item == Items.WITHER_SKELETON_SKULL) { // Special case skulls to allow wither spawns to be cancelled
                        BlockPos bp = blockPos;
                        if (!world.getBlockState(blockPos).canBeReplaced()) {
                            if (!world.getBlockState(blockPos).isSolid()) {
                                bp = null;
                            } else {
                                bp = bp.relative(context.getClickedFace());
                            }
                        }
                        if (bp != null) {
                            BlockEntity te = world.getBlockEntity(bp);
                            if (te instanceof SkullBlockEntity) {
                                WitherSkullBlock.checkSpawn(world, bp, (SkullBlockEntity) te);
                            }
                        }
                    }

                    // SPIGOT-4678
                    if (this.item instanceof SignItem && BukkitFieldHooks.openSign() != null) {
                        try {
                            if (world.getBlockEntity(BukkitFieldHooks.openSign()) instanceof SignBlockEntity tileentitysign) {
                                if (world.getBlockState(BukkitFieldHooks.openSign()).getBlock() instanceof SignBlock blocksign) {
                                    blocksign.pushOpenSignCause(PlayerSignOpenEvent.Cause.PLACE);
                                    blocksign.openTextEdit(player, tileentitysign, true);
                                }
                            }
                        } finally {
                            BukkitFieldHooks.setOpenSign(null);
                        }
                    }

                    // SPIGOT-7315: Moved from BlockBed#setPlacedBy
                    if (placeEvent != null && this.item instanceof BedItem) {
                        BlockPos position = ((CraftBlock) placeEvent.getBlock()).getPosition();
                        BlockState blockData =  world.getBlockState(position);

                        if (blockData.getBlock() instanceof BedBlock) {
                            world.blockUpdated(position, Blocks.AIR);
                            blockData.updateNeighbourShapes(world, position, 3);
                        }
                    }

                    // SPIGOT-1288 - play sound stripped from ItemBlock
                    if (this.item instanceof BlockItem) {
                        SoundType soundeffecttype = ((BlockItem) this.item).getBlock().defaultBlockState().getSoundType(); // TODO: not strictly correct, however currently only affects decorated pots
                        world.playSound(player, blockPos, soundeffecttype.getPlaceSound(), SoundSource.BLOCKS, (soundeffecttype.getVolume() + 1.0F) / 2.0F, soundeffecttype.getPitch() * 0.8F);
                    }
                    player.awardStat(Stats.ITEM_USED.get(item));
                }
                world.bridge$capturedTileEntities().clear();
                world.bridge$capturedBlockStates().clear();
                // CraftBukkit end
            }

            return interactionResult;
        }
    }

    // CraftBukkit start
    @Nullable
    @Override
    public PatchedDataComponentMap getComponentsClone() {
        return this.components.isEmpty() ? null : this.components.copy();
    }

    @Override
    public void setComponentsClone(@Nullable PatchedDataComponentMap patchedDataComponentMap) {
        this.applyComponents(patchedDataComponentMap == null ? null : patchedDataComponentMap.copy());
    }
    // CraftBukkit end
}
