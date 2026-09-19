package net.mcreator.spawnerheadmobs.procedures;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

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

		if (entity.level().isClientSide() || !(entity instanceof Mob hostMob)) {
			return;
		}

		CompoundTag nbt = hostMob.getPersistentData();
		if (!nbt.getBoolean(SpawnMobAsSpawnerHeadProcedure.IS_SPAWNER_HOST)) {
			return;
		}

		int timer = nbt.getInt(SpawnMobAsSpawnerHeadProcedure.SPAWNER_TIMER) - 1;

		if (timer <= 0) {
			// Reset tick timer
			nbt.putInt(SpawnMobAsSpawnerHeadProcedure.SPAWNER_TIMER, SpawnMobAsSpawnerHeadProcedure.SPAWN_INTERVAL_TICKS);

			if (hostMob.level() instanceof ServerLevel serverLevel) {
				String hostType = BuiltInRegistries.ENTITY_TYPE.getKey(hostMob.getType()).toString();
				
				// Roll for mob spawn selection based on config weightings
				String mobToSpawnId = ConfigProcedure.readConfigAndSelectMob(hostType);

				EntityType<?> spawnType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(mobToSpawnId));
				if (spawnType != null) {
					Entity spawnedEntity = spawnType.create(serverLevel);
					if (spawnedEntity instanceof Mob minion) {
						// Offset spawn location slightly next to the host mob (+/- 1 block radius)
						double offsetX = hostMob.getX() + (Math.random() - 0.5) * 2.0;
						double offsetY = hostMob.getY();
						double offsetZ = hostMob.getZ() + (Math.random() - 0.5) * 2.0;

						minion.moveTo(offsetX, offsetY, offsetZ, hostMob.getYRot(), hostMob.getXRot());
						
						// Finalize spawn so standard mob setup occurs
						minion.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(minion.blockPosition()), MobSpawnType.SPAWNER, null);

						// Spawn the new minion alongside the host mob
						serverLevel.addFreshEntity(minion);
					}
				}
			}
		} else {
			nbt.putInt(SpawnMobAsSpawnerHeadProcedure.SPAWNER_TIMER, timer);
		}
	}
}