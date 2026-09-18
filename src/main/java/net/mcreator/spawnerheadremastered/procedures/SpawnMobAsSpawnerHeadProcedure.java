package net.mcreator.spawnerheadremastered.procedures;

import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

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

import javax.annotation.Nullable;

@EventBusSubscriber
public class SpawnMobAsSpawnerHeadProcedure {
	@SubscribeEvent
	public static void onEntitySpawned(EntityJoinLevelEvent event) {
		execute(event);
	}

	public static void execute() {
		execute(null);
	}

	private static void execute(@Nullable Event event) {
		if (event == null) return;
		if (!(event instanceof EntityJoinLevelEvent spawnEvent)) return;

		Entity entity = spawnEvent.getEntity();
		// Ensure this code only runs server-side and applies to living entities (mobs)
		if (!spawnEvent.getLevel().isClientSide() && entity instanceof LivingEntity livingEntity) {
			String baseRegistryName = BuiltInRegistries.ENTITY_TYPE.getKey(livingEntity.getType()).toString();

			// 1. Roll the custom configurable spawner head spawn chance
			double spawnerHeadChance = ConfigProcedure.getSpawnerHeadChance();
			if (Math.random() < spawnerHeadChance) {
				
				// 2. Determine if this mob has a variant transformation set up in the config
				String finalMobRegistryName = ConfigProcedure.readConfigAndSelectMob(baseRegistryName);
				
				LivingEntity targetMob = livingEntity;

				// 3. If a different mob ID was chosen by the weighted random logic, replace the mob
				if (!finalMobRegistryName.equals(baseRegistryName)) {
					EntityType<?> targetType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(finalMobRegistryName));
					if (targetType != null) {
						Entity newEntity = targetType.create(spawnEvent.getLevel());
						if (newEntity instanceof LivingEntity newLiving) {
							// Match positioning and rotation
							newLiving.moveTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), livingEntity.getYRot(), livingEntity.getXRot());
							
							if (newLiving instanceof Mob mobEntity && spawnEvent.getLevel() instanceof ServerLevel serverLevel) {
								mobEntity.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mobEntity.blockPosition()), MobSpawnType.NATURAL, null);
							}
							
							// Add it to the world and discard the old base mob
							spawnEvent.getLevel().addFreshEntity(newLiving);
							livingEntity.discard();
							targetMob = newLiving;
						}
					}
				}

				// 4. Equip a Spawner Block visual onto the mob's head slot
				ItemStack spawnerItem = new ItemStack(Blocks.SPAWNER);
				targetMob.setItemSlot(EquipmentSlot.HEAD, spawnerItem);
				
				// Optional: Ensure the mob doesn't drop the spawner when killed
				if (targetMob instanceof Mob mob) {
					mob.setDropChance(EquipmentSlot.HEAD, 0.0F);
				}
			}
		}
	}
}
