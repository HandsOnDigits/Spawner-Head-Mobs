package net.mcreator.spawnerheadremastered.procedures;

import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigProcedure {
	private static final Random RANDOM = new Random();
	private static boolean initialized = false;
	private static double cachedSpawnChance = 0.05;
	private static String cachedFileContent = "";

	public static void execute() {
		setupFileOnDisk();
	}

	public static synchronized void setupFileOnDisk() {
		try {
			// Explicitly target the JSON file path inside the config directory
			Path configPath = FMLPaths.CONFIGDIR.get().resolve("spawner_head_remastered.json");
			File targetFile = configPath.toFile();

			// Ensure parent folder exists
			File parentDir = targetFile.getParentFile();
			if (parentDir != null && !parentDir.exists()) {
				parentDir.mkdirs();
			}

			// Write default file content ONLY if the JSON file does not exist yet
			if (!targetFile.exists()) {
				String jsonContent = """
					{
					  "global_settings": {
					    "spawner_head_spawn_chance": 0.05
					  },
					  "mobs_with_head_block_support": [
					    {
					      "id": "minecraft:zombie",
					      "random_mobs_to_spawn": [
					        { "id": "minecraft:silverfish", "weight": 50 },
					        { "id": "minecraft:zombie", "weight": 30 },
					        { "id": "minecraft:husk", "weight": 10 },
					        { "id": "minecraft:spider", "weight": 10 }
					      ]
					    },
					    {
					      "id": "minecraft:skeleton",
					      "random_mobs_to_spawn": [
					        { "id": "minecraft:silverfish", "weight": 50 },
					        { "id": "minecraft:skeleton", "weight": 30 },
					        { "id": "minecraft:stray", "weight": 10 },
					        { "id": "minecraft:spider", "weight": 10 }
					      ]
					    },
					    {
					      "id": "minecraft:drowned",
					      "random_mobs_to_spawn": [
					        { "id": "minecraft:tadpole", "weight": 50 },
					        { "id": "minecraft:drowned", "weight": 30 },
					        { "id": "minecraft:pufferfish", "weight": 20 }
					        
					      ]
					    },
					    {
					      "id": "minecraft:husk",
					      "random_mobs_to_spawn": [
					        { "id": "minecraft:silverfish", "weight": 50 },
					        { "id": "minecraft:husk", "weight": 30 },
					        { "id": "minecraft:zombie", "weight": 20 }
					      ]
					    },
					    {
					      "id": "minecraft:stray",
					      "random_mobs_to_spawn": [
					        { "id": "minecraft:silverfish", "weight": 50 },
					        { "id": "minecraft:stray", "weight": 30 },
					        { "id": "minecraft:skeleton", "weight": 20 }
					      ]
					    },
					    {
					      "id": "minecraft:piglin",
					      "random_mobs_to_spawn": [
					        { "id": "minecraft:magma_cube", "weight": 50 },
					        { "id": "minecraft:piglin", "weight": 30 },
					        { "id": "minecraft:silverfish", "weight": 20 }
					      ]
					    },
					    {
					      "id": "minecraft:piglin_brute",
					      "random_mobs_to_spawn": [
					        { "id": "minecraft:magma_cube", "weight": 50 },
					        { "id": "minecraft:piglin_brute", "weight": 30 },
					        { "id": "minecraft:hoglin", "weight": 20 }
					      ]
					    },
					    {
					      "id": "minecraft:wither_skeleton",
					      "random_mobs_to_spawn": [
					        { "id": "minecraft:wither_skeleton", "weight": 50 },
					        { "id": "minecraft:magma_cube", "weight": 30 },
					        { "id": "minecraft:skeleton", "weight": 20 }
					      ]
					    }
					  ]
					}
					""";

				Files.writeString(
					configPath, 
					jsonContent, 
					StandardOpenOption.CREATE_NEW, 
					StandardOpenOption.WRITE
				);
			}

			// Cache values in memory so we never read/write during mob spawns again
			if (targetFile.exists() && targetFile.isFile()) {
				cachedFileContent = Files.readString(configPath);
				Pattern pattern = Pattern.compile("\"spawner_head_spawn_chance\"\\s*:\\s*([0-9.]+)");
				Matcher matcher = pattern.matcher(cachedFileContent);
				if (matcher.find()) {
					cachedSpawnChance = Double.parseDouble(matcher.group(1));
				}
			}

			initialized = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static synchronized void ensureInitialized() {
		if (!initialized) {
			setupFileOnDisk();
		}
	}

	public static double getSpawnerHeadChance() {
		ensureInitialized();
		return cachedSpawnChance;
	}

	public static String readConfigAndSelectMob(String baseMobId) {
		ensureInitialized();

		if (cachedFileContent.isEmpty()) {
			return baseMobId;
		}

		try {
			Pattern entryPattern = Pattern.compile("\\{\\s*\"id\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"random_mobs_to_spawn\"\\s*:\\s*\\[(.*?)\\]\\s*\\}", Pattern.DOTALL);
			Matcher entryMatcher = entryPattern.matcher(cachedFileContent);

			while (entryMatcher.find()) {
				String configMobId = entryMatcher.group(1);
				if (configMobId.equals(baseMobId)) {
					String arrayContent = entryMatcher.group(2);
					Pattern optionPattern = Pattern.compile("\\{\\s*\"id\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"weight\"\\s*:\\s*(\\d+)\\s*\\}");
					Matcher optionMatcher = optionPattern.matcher(arrayContent);

					List<MobWeightEntry> entries = new ArrayList<>();
					int totalWeight = 0;

					while (optionMatcher.find()) {
						String targetId = optionMatcher.group(1);
						int weight = Integer.parseInt(optionMatcher.group(2));
						entries.add(new MobWeightEntry(targetId, weight));
						totalWeight += weight;
					}

					if (totalWeight > 0) {
						int targetWeight = RANDOM.nextInt(totalWeight);
						for (MobWeightEntry entry : entries) {
							targetWeight -= entry.weight;
							if (targetWeight < 0) {
								return entry.id;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return baseMobId;
	}

	private static class MobWeightEntry {
		final String id;
		final int weight;

		MobWeightEntry(String id, int weight) {
			this.id = id;
			this.weight = weight;
		}
	}
}