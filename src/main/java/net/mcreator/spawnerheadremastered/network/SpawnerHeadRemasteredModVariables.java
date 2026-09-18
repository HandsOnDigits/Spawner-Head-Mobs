package net.mcreator.spawnerheadremastered.network;

import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.mcreator.spawnerheadremastered.SpawnerHeadRemasteredMod;

import java.io.File;

@EventBusSubscriber
public class SpawnerHeadRemasteredModVariables {
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SpawnerHeadRemasteredMod.MODID);
	public static File Config = new File("");

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
	}
}