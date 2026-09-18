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

	public static final String SPAWNER_HEAD_TAG = "IsSpawnerHeadMob";
	public static final String TIMER_TAG = "SpawnerHeadTimer";
	public static final String BASE_TYPE_TAG = "SpawnerHeadBaseType";

	// Interval between mob swaps in ticks (20 ticks = 1 second, 100 ticks = 5 seconds)
	public static final int SWAP_INTERVAL_TICKS = 100;

	@SubscribeEvent
	public static void onEntityFinalizeSpawn(FinalizeSpawnEvent event) {
		Mob mob = event.getEntity();

		if (event.getLevel().isClientSide() || mob.getPersistentData().getBoolean(SPAWNER_HEAD_TAG)) {
			return;
		}

		double spawnerHeadChance = ConfigProcedure.getSpawnerHeadChance();
		if (Math.random() < spawnerHeadChance) {
			// Mark entity as a spawner head mob and track original base ID
			mob.getPersistentData().putBoolean(SPAWNER_HEAD_TAG, true);
			mob.getPersistentData().putInt(TIMER_TAG, SWAP_INTERVAL_TICKS);
			
			String baseRegistryName = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
			mob.getPersistentData().putString(BASE_TYPE_TAG, baseRegistryName);

			// Equip spawner block
			mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Blocks.SPAWNER));
			mob.setDropChance(EquipmentSlot.HEAD, 0.0F);
		}
	}
}