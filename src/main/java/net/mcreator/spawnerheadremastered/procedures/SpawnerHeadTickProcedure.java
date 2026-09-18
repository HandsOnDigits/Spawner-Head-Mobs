package net.mcreator.spawnerheadremastered.procedures;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;

@EventBusSubscriber
public class SpawnerHeadTickProcedure {

	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Post event) {
		Entity entity = event.getEntity();

		if (entity.level().isClientSide() || !(entity instanceof Mob mob)) {
			return;
		}

		CompoundTag nbt = mob.getPersistentData();
		if (!nbt.getBoolean(SpawnMobAsSpawnerHeadProcedure.SPAWNER_HEAD_TAG)) {
			return;
		}

		int timer = nbt.getInt(SpawnMobAsSpawnerHeadProcedure.TIMER_TAG) - 1;

		if (timer <= 0) {
			// Reset timer
			nbt.putInt(SpawnMobAsSpawnerHeadProcedure.TIMER_TAG, SpawnMobAsSpawnerHeadProcedure.SWAP_INTERVAL_TICKS);

			String baseType = nbt.getString(SpawnMobAsSpawnerHeadProcedure.BASE_TYPE_TAG);
			String currentType = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
			String nextType = ConfigProcedure.readConfigAndSelectMob(baseType);

			// Swap entity if weighted chance rolled a different mob type
			if (!nextType.equals(currentType) && mob.level() instanceof ServerLevel serverLevel) {
				EntityType<?> targetType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(nextType));
				if (targetType != null) {
					Entity newEntity = targetType.create(serverLevel);
					if (newEntity instanceof Mob newMob) {
						// Transfer persistent state to the new mob
						newMob.getPersistentData().putBoolean(SpawnMobAsSpawnerHeadProcedure.SPAWNER_HEAD_TAG, true);
						newMob.getPersistentData().putInt(SpawnMobAsSpawnerHeadProcedure.TIMER_TAG, SpawnMobAsSpawnerHeadProcedure.SWAP_INTERVAL_TICKS);
						newMob.getPersistentData().putString(SpawnMobAsSpawnerHeadProcedure.BASE_TYPE_TAG, baseType);

						// Position, health, and head equipment transfer
						newMob.moveTo(mob.getX(), mob.getY(), mob.getZ(), mob.getYRot(), mob.getXRot());
						newMob.setHealth(Math.min(mob.getHealth(), newMob.getMaxHealth()));
						newMob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Blocks.SPAWNER));
						newMob.setDropChance(EquipmentSlot.HEAD, 0.0F);

						newMob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(newMob.blockPosition()), MobSpawnType.CONVERSION, null);

						mob.discard();
						serverLevel.addFreshEntity(newMob);
					}
				}
			}
		} else {
			nbt.putInt(SpawnMobAsSpawnerHeadProcedure.TIMER_TAG, timer);
		}
	}
}