package net.mcreator.spawnerheadmobs.procedures;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

@EventBusSubscriber
public class SpawnerHeadParticalEmitterProcedure {

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();

        if (entity.level().isClientSide() || !(entity instanceof Mob mob)) {
            return;
        }

        if (mob.getPersistentData().getBoolean(SpawnMobAsSpawnerHeadProcedure.IS_SPAWNER_HOST)) {
            // Spawn particles once every 5 ticks (~4 times a second) instead of every single tick
            if (mob.tickCount % 5 == 0 && mob.level() instanceof ServerLevel serverLevel) {
                double x = mob.getX();
                double y = mob.getY() + mob.getEyeHeight();
                double z = mob.getZ();

                // Emits just 1 subtle flame particle with minimal spread
                serverLevel.sendParticles(
                    ParticleTypes.FLAME, 
                    x, y, z, 
                    1, 
                    0.1, 0.1, 0.1, 0.01
                );
            }
        }
    }
}