package com.mohistmc.banner.mixin.core.world.item;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import com.mohistmc.banner.injection.world.item.InjectionItemStack;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements InjectionItemStack {

    // @formatter:off
    @Shadow @Deprecated private Item item;
    @Shadow private int count;
    // @formatter:on
    @Shadow public abstract CompoundTag save(CompoundTag compoundTag);
    @Shadow @Nullable private CompoundTag tag;

    @Shadow public abstract Item getItem();

    @Shadow public abstract void setDamageValue(int damage);

    @Shadow public abstract int getDamageValue();

    @Shadow public abstract void setTag(@Nullable CompoundTag compoundTag);

    @Shadow public abstract void removeTagKey(String key);

    @Shadow public abstract int getCount();

    @Shadow public abstract void setCount(int count);

    @Shadow public abstract boolean hasAdventureModePlaceTagForBlock(Registry<Block> blockRegistry, BlockInWorld block);

    @Shadow public abstract ItemStack copy();

    @Shadow public abstract void shrink(int decrement);

    @Redirect(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;getCompound(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag banner$markAsCopy(CompoundTag instance, String key) {
        // CraftBukkit start - make defensive copy as this data may be coming from the save thread
        return instance.getCompound(key).copy();
        // CraftBukkit end
    }

    @Override
    public void convertStack(int version) {
        if (0 < version && version < CraftMagicNumbers.INSTANCE.getDataVersion()) {
            CompoundTag savedStack = new CompoundTag();
            this.save(savedStack);
            savedStack = (CompoundTag) BukkitExtraConstants.getServer().fixerUpper.update(References.ITEM_STACK, new Dynamic(NbtOps.INSTANCE, savedStack), version, CraftMagicNumbers.INSTANCE.getDataVersion()).getValue();
            this.load(savedStack);
        }
    }

    @Inject(at = @At("HEAD"), method = "hurt", cancellable = true)
    public void banner$callPlayerItemDamageEvent(int i, RandomSource random, ServerPlayer entityplayer, CallbackInfoReturnable<Boolean> ci) {
        if (!((ItemStack)(Object)this).isDamageableItem()) {
            ci.setReturnValue(false);
            return;
        }
        int j;

        if (i > 0) {
            j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, ((ItemStack)(Object)this));
            for (int l = 0; j > 0 && l < i; ++l) if (DigDurabilityEnchantment.shouldIgnoreDurabilityDrop(((ItemStack)(Object)this), j, random)) i--;

            if (entityplayer != null) {
                PlayerItemDamageEvent event = new PlayerItemDamageEvent(((ServerPlayer)entityplayer).getBukkitEntity(), CraftItemStack.asCraftMirror((ItemStack)(Object)this), i);
                event.getPlayer().getServer().getPluginManager().callEvent(event);

                if (i != event.getDamage() || event.isCancelled()) event.getPlayer().updateInventory();
                if (event.isCancelled()) {
                    ci.setReturnValue(false);
                    return;
                }
                i = event.getDamage();
            }
            if (i <= 0) {
                ci.setReturnValue(false);
                return;
            }
        }
        if (entityplayer != null && i != 0) CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(entityplayer, ((ItemStack)(Object)this), ((ItemStack)(Object)this).getDamageValue() + i);

        ((ItemStack)(Object)this).setDamageValue((j = ((ItemStack)(Object)this).getDamageValue() + i));
        ci.setReturnValue(j >= ((ItemStack)(Object)this).getMaxDamage());
        return;
    }

    @Inject(method = "hurtAndBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private <T extends LivingEntity> void banner$itemBreak(int amount, T entityIn, Consumer<T> onBroken, CallbackInfo ci) {
        if (this.count == 1 && entityIn instanceof Player) {
            CraftEventFactory.callPlayerItemBreakEvent(((Player) entityIn), (ItemStack) (Object) this);
        }
    }

    @Override
    public void load(CompoundTag compoundTag) {
        this.item = BuiltInRegistries.ITEM.get(new ResourceLocation(compoundTag.getString("id")));
        this.count = compoundTag.getByte("Count");
        if (compoundTag.contains("tag", 10)) {
            // CraftBukkit start - make defensive copy as this data may be coming from the save thread
            this.tag = compoundTag.getCompound("tag").copy();
            // CraftBukkit end
            this.getItem().verifyTagAfterLoad(this.tag);
        }

        if (this.getItem().canBeDepleted()) {
            this.setDamageValue(this.getDamageValue());
        }
    }

    @Override
    @Nullable
    public CompoundTag getTagClone() {
        return this.tag == null ? null : this.tag.copy();
    }

    @Override
    public void setTagClone(@Nullable CompoundTag nbtttagcompound) {
        this.setTag(nbtttagcompound == null ? null : nbtttagcompound.copy());
    }

    @Inject(method = "setRepairCost", at = @At("HEAD"), cancellable = true)
    private void banner$handleCost(int cost, CallbackInfo ci) {
        // CraftBukkit start - remove RepairCost tag when 0 (SPIGOT-3945)
        if (cost == 0) {
            this.removeTagKey("RepairCost");
            ci.cancel();
        }
        // CraftBukkit end
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
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        BlockPos blockPos = context.getClickedPos();
        BlockInWorld blockInWorld = new BlockInWorld(context.getLevel(), blockPos, false);
        if (player != null && !player.getAbilities().mayBuild && !this.hasAdventureModePlaceTagForBlock(context.getLevel().registryAccess().registryOrThrow(Registries.BLOCK), blockInWorld)) {
            return InteractionResult.PASS;
        } else {
            Item item = this.getItem();
            // CraftBukkit start - handle all block place event logic here
            CompoundTag oldData = this.getTagClone();
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
            CompoundTag newData = this.getTagClone();
            int newCount = this.getCount();
            this.setCount(oldCount);
            this.setTagClone(oldData);
            if (interactionResult.consumesAction() && world.bridge$captureTreeGeneration() && !world.bridge$capturedBlockStates().isEmpty()) {
                world.banner$setCaptureTreeGeneration(false);
                Location location = CraftLocation.toBukkit(blockPos, world.getWorld());
                TreeType treeType = BukkitExtraConstants.treeType;
                BukkitExtraConstants.treeType = null;
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
                    if (this.getCount() == oldCount && Objects.equals(this.tag, oldData)) {
                        this.setTag(newData);
                        this.setCount(newCount);
                    }
                    for (CraftBlockState blockstate : blocks) {
                        world.setBlock(blockstate.getPosition(),blockstate.getHandle(), blockstate.getFlag()); // SPIGOT-7248 - manual update to avoid physics where appropriate
                    }
                    player.awardStat(Stats.ITEM_USED.get(item)); // SPIGOT-7236 - award stat
                }
                BukkitExtraConstants.openSign = null; // SPIGOT-6758 - Reset on early return // Banner - cancel
                return interactionResult;
            }
            world.banner$setCaptureTreeGeneration(false);
            if (player != null && interactionResult.shouldAwardStats()) {
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
                    BukkitExtraConstants.openSign = null; // SPIGOT-6758 - Reset on early return // Banner - cancel
                } else {
                    // Change the stack to its new contents if it hasn't been tampered with.
                    if (this.getCount() == oldCount && Objects.equals(this.tag, oldData)) {
                        this.setTag(newData);
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

                    // Special case juke boxes as they update their tile entity. Copied from ItemRecord.
                    // PAIL: checkme on updates.
                    if (this.item instanceof RecordItem) {
                        BlockEntity tileentity = world.getBlockEntity(blockPos);

                        if (tileentity instanceof JukeboxBlockEntity tileentityjukebox) {

                            // There can only be one
                            ItemStack record = this.copy();
                            if (!record.isEmpty()) {
                                record.setCount(1);
                            }

                            tileentityjukebox.setTheItem(record);
                            world.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, world.getBlockState(blockPos)));
                        }

                        this.shrink(1);
                        player.awardStat(Stats.PLAY_RECORD);
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
                    if (this.item instanceof SignItem && BukkitExtraConstants.openSign != null) {
                        try {
                            if (world.getBlockEntity(BukkitExtraConstants.openSign) instanceof SignBlockEntity tileentitysign) {
                                if (world.getBlockState(BukkitExtraConstants.openSign).getBlock() instanceof SignBlock blocksign) {
                                    blocksign.pushOpenSignCause(PlayerSignOpenEvent.Cause.PLACE);
                                    blocksign.openTextEdit(player, tileentitysign, true);
                                }
                            }
                        } finally {
                            BukkitExtraConstants.openSign = null;
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
}
