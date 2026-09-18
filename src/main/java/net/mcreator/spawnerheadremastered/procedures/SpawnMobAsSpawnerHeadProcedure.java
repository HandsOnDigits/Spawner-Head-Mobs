package net.mcreator.spawnerheadremastered.procedures;

import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;

@EventBusSubscriber
public class SpawnMobAsSpawnerHeadProcedure {

	public static final String IS_SPAWNER_HOST = "IsSpawnerHeadHost";
	public static final String SPAWNER_TIMER = "SpawnerHeadTimer";
	
	// Interval between spawns in ticks (100 ticks = 5 seconds)
	public static final int SPAWN_INTERVAL_TICKS = 100;

	@SubscribeEvent
	public static void onEntityFinalizeSpawn(FinalizeSpawnEvent event) {
		Mob mob = event.getEntity();

		if (event.getLevel().isClientSide() || mob.getPersistentData().getBoolean(IS_SPAWNER_HOST)) {
			return;
		}

		double spawnerHeadChance = ConfigProcedure.getSpawnerHeadChance();
		if (Math.random() < spawnerHeadChance) {
			// Mark as a host entity that spawns other mobs
			mob.getPersistentData().putBoolean(IS_SPAWNER_HOST, true);
			mob.getPersistentData().putInt(SPAWNER_TIMER, SPAWN_INTERVAL_TICKS);

			// Equip spawner block visual on host's head
			mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Blocks.SPAWNER));
			mob.setDropChance(EquipmentSlot.HEAD, 0.0F);
		}
	}
}