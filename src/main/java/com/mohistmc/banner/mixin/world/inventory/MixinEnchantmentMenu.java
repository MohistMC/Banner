package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryEnchanting;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(EnchantmentMenu.class)
public abstract class MixinEnchantmentMenu extends AbstractContainerMenu{

    // @formatter:off
    @Shadow @Final private Container enchantSlots;
    @Shadow @Final private ContainerLevelAccess access;
    @Shadow @Final private RandomSource random;
    @Shadow @Final private DataSlot enchantmentSeed;
    @Shadow @Final public int[] costs;
    @Shadow @Final public int[] enchantClue;
    @Shadow @Final public int[] levelClue;
    @Shadow protected abstract List<EnchantmentInstance> getEnchantmentList(ItemStack stack, int enchantSlot, int level);
    // @formatter:on

    private CraftInventoryView bukkitEntity = null;
    private org.bukkit.entity.Player player;

    protected MixinEnchantmentMenu(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    public void banner$init(int id, Inventory playerInventory, ContainerLevelAccess worldPosCallable, CallbackInfo ci) {
        this.player = (org.bukkit.entity.Player) playerInventory.player.getBukkitEntity();
    }

    @Inject(method = "stillValid", cancellable = true, at = @At("HEAD"))
    public void banner$unreachable(net.minecraft.world.entity.player.Player playerIn, CallbackInfoReturnable<Boolean> cir) {
        if (!bridge$checkReachable()) cir.setReturnValue(true);
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public boolean clickMenuButton(net.minecraft.world.entity.player.Player player, int id) {
        if (id >= 0 && id < this.costs.length) {
            ItemStack itemStack = this.enchantSlots.getItem(0);
            ItemStack itemStack2 = this.enchantSlots.getItem(1);
            int i = id + 1;
            if ((itemStack2.isEmpty() || itemStack2.getCount() < i) && !player.getAbilities().instabuild) {
                return false;
            } else if (this.costs[id] <= 0 || itemStack.isEmpty() || (player.experienceLevel < i || player.experienceLevel < this.costs[id]) && !player.getAbilities().instabuild) {
                return false;
            } else {
                this.access.execute((level, blockPos) -> {
                    ItemStack itemStack3 = itemStack;
                    List<EnchantmentInstance> list = this.getEnchantmentList(itemStack, id, this.costs[id]);
                    if (true || !list.isEmpty()) {
                        //player.onEnchantmentPerformed(itemStack, i);
                        boolean bl = itemStack.is(Items.BOOK);
                        Map<org.bukkit.enchantments.Enchantment, Integer> enchants = new java.util.HashMap<>();
                        for (Object obj : list) {
                            EnchantmentInstance instance = (EnchantmentInstance) obj;
                            enchants.put(org.bukkit.enchantments.Enchantment.getByKey(CraftNamespacedKey.fromMinecraft(BuiltInRegistries.ENCHANTMENT.getKey(instance.enchantment))), instance.level);
                        }
                        CraftItemStack item = CraftItemStack.asCraftMirror(itemStack3);

                        org.bukkit.enchantments.Enchantment hintedEnchantment = org.bukkit.enchantments.Enchantment.getByKey(CraftNamespacedKey.fromMinecraft(BuiltInRegistries.ENCHANTMENT.getKey(Enchantment.byId(enchantClue[id]))));
                        int hintedEnchantmentLevel = levelClue[id];
                        EnchantItemEvent event = new EnchantItemEvent((org.bukkit.entity.Player) player.getBukkitEntity(), this.getBukkitView(), access.getLocation().getBlock(), item, this.costs[id], enchants, hintedEnchantment, hintedEnchantmentLevel, id);
                        level.getCraftServer().getPluginManager().callEvent(event);

                        int banner$level = event.getExpLevelCost();
                        if (event.isCancelled() || (banner$level > player.experienceLevel && !player.getAbilities().instabuild) || event.getEnchantsToAdd().isEmpty()) {
                            return;
                        }
                        if (bl) {
                            itemStack3 = new ItemStack(Items.ENCHANTED_BOOK);
                            CompoundTag compoundTag = itemStack.getTag();
                            if (compoundTag != null) {
                                itemStack3.setTag(compoundTag.copy());
                            }

                            this.enchantSlots.setItem(0, itemStack3);
                        }

                        for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : event.getEnchantsToAdd().entrySet()) {
                            try {
                                if (bl) {
                                    NamespacedKey enchantId = entry.getKey().getKey();
                                    Enchantment nms = BuiltInRegistries.ENCHANTMENT.get(CraftNamespacedKey.toMinecraft(enchantId));
                                    if (nms == null) {
                                        continue;
                                    }

                                    EnchantmentInstance weightedrandomenchant = new EnchantmentInstance(nms, entry.getValue());
                                    EnchantedBookItem.addEnchantment(itemStack3, weightedrandomenchant);
                                } else {
                                    item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
                                }
                            } catch (IllegalArgumentException e) {
                                /* Just swallow invalid enchantments */
                            }
                        }

                        player.onEnchantmentPerformed(itemStack, i);
                        // CraftBukkit end

                        // CraftBukkit - TODO: let plugins change this
                        if (!player.getAbilities().instabuild) {
                            itemStack2.shrink(i);
                            if (itemStack2.isEmpty()) {
                                this.enchantSlots.setItem(1, ItemStack.EMPTY);
                            }
                        }

                        player.awardStat(Stats.ENCHANT_ITEM);
                        if (player instanceof ServerPlayer) {
                            CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer)player, itemStack3, i);
                        }

                        this.enchantSlots.setChanged();
                        this.enchantmentSeed.set(player.getEnchantmentSeed());
                        this.slotsChanged(this.enchantSlots);
                        level.playSound((Player)null, blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
                    }

                });
                return true;
            }
        } else {
            Component var10000 = player.getName();
            Util.logAndPauseIfInIde("" + var10000 + " pressed invalid button id: " + id);
            return false;
        }
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void slotsChanged(Container container) {
        if (container == this.enchantSlots) {
            ItemStack itemStack = container.getItem(0);
            if (!itemStack.isEmpty() && itemStack.isEnchantable()) {
                this.access.execute((level, blockPos) -> {
                    int i = 0;

                    for (BlockPos blockPos2 : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
                        if (EnchantmentTableBlock.isValidBookShelf(level, blockPos, blockPos2)) {
                            ++i;
                        }
                    }

                    this.random.setSeed((long)this.enchantmentSeed.get());

                    int j;
                    for(j = 0; j < 3; ++j) {
                        this.costs[j] = EnchantmentHelper.getEnchantmentCost(this.random, j, i, itemStack);
                        this.enchantClue[j] = -1;
                        this.levelClue[j] = -1;
                        if (this.costs[j] < j + 1) {
                            this.costs[j] = 0;
                        }
                    }

                    for(j = 0; j < 3; ++j) {
                        if (this.costs[j] > 0) {
                            List<EnchantmentInstance> list = this.getEnchantmentList(itemStack, j, this.costs[j]);
                            if (list != null && !list.isEmpty()) {
                                EnchantmentInstance enchantmentInstance = (EnchantmentInstance)list.get(this.random.nextInt(list.size()));
                                this.enchantClue[j] = BuiltInRegistries.ENCHANTMENT.getId(enchantmentInstance.enchantment);
                                this.levelClue[j] = enchantmentInstance.level;
                            }
                        }
                    }

                    // CraftBukkit start
                    CraftItemStack item = CraftItemStack.asCraftMirror(itemStack);
                    org.bukkit.enchantments.EnchantmentOffer[] offers = new EnchantmentOffer[3];
                    for (j = 0; j < 3; ++j) {
                        org.bukkit.enchantments.Enchantment enchantment = (this.enchantClue[j] >= 0) ? org.bukkit.enchantments.Enchantment.getByKey(CraftNamespacedKey.fromMinecraft(BuiltInRegistries.ENCHANTMENT.getKey(BuiltInRegistries.ENCHANTMENT.byId(this.enchantClue[j])))) : null;
                        offers[j] = (enchantment != null) ? new EnchantmentOffer(enchantment, this.levelClue[j], this.costs[j]) : null;
                    }

                    PrepareItemEnchantEvent event = new PrepareItemEnchantEvent(player, this.getBukkitView(), access.getLocation().getBlock(), item, offers, i);
                    event.setCancelled(!itemStack.isEnchantable());
                    level.getCraftServer().getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        for (j = 0; j < 3; ++j) {
                            this.costs[j] = 0;
                            this.enchantClue[j] = -1;
                            this.levelClue[j] = -1;
                        }
                        return;
                    }

                    for (j = 0; j < 3; j++) {
                        EnchantmentOffer offer = event.getOffers()[j];
                        if (offer != null) {
                            this.costs[j] = offer.getCost();
                            this.enchantClue[j] = BuiltInRegistries.ENCHANTMENT.getId(BuiltInRegistries.ENCHANTMENT.get(CraftNamespacedKey.toMinecraft(offer.getEnchantment().getKey())));
                            this.levelClue[j] = offer.getEnchantmentLevel();
                        } else {
                            this.costs[j] = 0;
                            this.enchantClue[j] = -1;
                            this.levelClue[j] = -1;
                        }
                        // CraftBukkit end
                    }
                    this.broadcastChanges();
                });
            } else {
                for(int i = 0; i < 3; ++i) {
                    this.costs[i] = 0;
                    this.enchantClue[i] = -1;
                    this.levelClue[i] = -1;
                }
            }
        }

    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventoryEnchanting inventory = new CraftInventoryEnchanting(this.enchantSlots);
        bukkitEntity = new CraftInventoryView(this.player, inventory, (AbstractContainerMenu) (Object) this);
        return bukkitEntity;
    }
}
