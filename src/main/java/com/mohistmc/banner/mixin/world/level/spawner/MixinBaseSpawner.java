package com.mohistmc.banner.mixin.world.level.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(BaseSpawner.class)
public abstract class MixinBaseSpawner {


    // @formatter:off
    @Shadow public SimpleWeightedRandomList<SpawnData> spawnPotentials;
    @Shadow public int spawnDelay;
    @Shadow public int spawnCount;
    @Shadow public int spawnRange;
    @Shadow public int maxNearbyEntities;
    @Shadow protected abstract boolean isNearPlayer(Level level, BlockPos pos);
    @Shadow protected abstract void delay(Level level, BlockPos pos);
    @Shadow protected abstract SpawnData getOrCreateNextSpawnData(@Nullable Level level, RandomSource random, BlockPos pos);
    // @formatter:on

    @Inject(method = "setEntityId", at = @At("RETURN"))
    public void banner$clearMobs(CallbackInfo ci) {
        this.spawnPotentials = SimpleWeightedRandomList.empty();
    }

    /**
     * @author wdog5
     * @reason bukkit things
     */
    @Overwrite
    public void serverTick(ServerLevel worldserver, BlockPos blockposition) {
        if (this.isNearPlayer(worldserver, blockposition)) {
            if (this.spawnDelay == -1) {
                this.delay(worldserver, blockposition);
            }

            if (this.spawnDelay > 0) {
                --this.spawnDelay;
            } else {
                boolean flag = false;
                RandomSource randomsource = worldserver.getRandom();
                SpawnData mobspawnerdata = this.getOrCreateNextSpawnData(worldserver, randomsource, blockposition);

                for (int i = 0; i < this.spawnCount; ++i) {
                    CompoundTag nbttagcompound = mobspawnerdata.getEntityToSpawn();
                    Optional<EntityType<?>> optional = EntityType.by(nbttagcompound);

                    if (optional.isEmpty()) {
                        this.delay(worldserver, blockposition);
                        return;
                    }

                    ListTag nbttaglist = nbttagcompound.getList("Pos", 6);
                    int j = nbttaglist.size();
                    double d0 = j >= 1 ? nbttaglist.getDouble(0) : (double) blockposition.getX() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double) this.spawnRange + 0.5D;
                    double d1 = j >= 2 ? nbttaglist.getDouble(1) : (double) (blockposition.getY() + randomsource.nextInt(3) - 1);
                    double d2 = j >= 3 ? nbttaglist.getDouble(2) : (double) blockposition.getZ() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double) this.spawnRange + 0.5D;

                    if (worldserver.noCollision(((EntityType) optional.get()).getAABB(d0, d1, d2))) {
                        BlockPos blockposition1 = BlockPos.containing(d0, d1, d2);

                        if (mobspawnerdata.getCustomSpawnRules().isPresent()) {
                            if (!((EntityType) optional.get()).getCategory().isFriendly() && worldserver.getDifficulty() == Difficulty.PEACEFUL) {
                                continue;
                            }

                            SpawnData.CustomSpawnRules mobspawnerdata_a = (SpawnData.CustomSpawnRules) mobspawnerdata.getCustomSpawnRules().get();

                            if (!mobspawnerdata_a.blockLightLimit().isValueInRange(worldserver.getBrightness(LightLayer.BLOCK, blockposition1)) || !mobspawnerdata_a.skyLightLimit().isValueInRange(worldserver.getBrightness(LightLayer.SKY, blockposition1))) {
                                continue;
                            }
                        } else if (!SpawnPlacements.checkSpawnRules((EntityType) optional.get(), worldserver, MobSpawnType.SPAWNER, blockposition1, worldserver.getRandom())) {
                            continue;
                        }

                        Entity entity = EntityType.loadEntityRecursive(nbttagcompound, worldserver, (entity1) -> {
                            entity1.moveTo(d0, d1, d2, entity1.getYRot(), entity1.getXRot());
                            return entity1;
                        });

                        if (entity == null) {
                            this.delay(worldserver, blockposition);
                            return;
                        }

                        int k = worldserver.getEntitiesOfClass(entity.getClass(), (new AABB((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), (double) (blockposition.getX() + 1), (double) (blockposition.getY() + 1), (double) (blockposition.getZ() + 1))).inflate((double) this.spawnRange)).size();

                        if (k >= this.maxNearbyEntities) {
                            this.delay(worldserver, blockposition);
                            return;
                        }

                        entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), randomsource.nextFloat() * 360.0F, 0.0F);
                        if (entity instanceof Mob) {
                            Mob entityinsentient = (Mob) entity;

                            if (mobspawnerdata.getCustomSpawnRules().isEmpty() && !entityinsentient.checkSpawnRules(worldserver, MobSpawnType.SPAWNER) || !entityinsentient.checkSpawnObstruction(worldserver)) {
                                continue;
                            }

                            if (mobspawnerdata.getEntityToSpawn().size() == 1 && mobspawnerdata.getEntityToSpawn().contains("id", 8)) {
                                ((Mob) entity).finalizeSpawn(worldserver, worldserver.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.SPAWNER, (SpawnGroupData) null, (CompoundTag) null);
                            }
                        }

                        if (!worldserver.tryAddFreshEntityWithPassengers(entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SPAWNER)) { // CraftBukkit
                            this.delay(worldserver, blockposition);
                            return;
                        }

                        worldserver.levelEvent(2004, blockposition, 0);
                        worldserver.gameEvent(entity, GameEvent.ENTITY_PLACE, blockposition1);
                        if (entity instanceof Mob) {
                            ((Mob) entity).spawnAnim();
                        }

                        flag = true;
                    }
                }

                if (flag) {
                    this.delay(worldserver, blockposition);
                }

            }
        }
    }
}
