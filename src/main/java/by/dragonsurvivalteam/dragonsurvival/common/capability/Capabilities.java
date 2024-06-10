package by.dragonsurvivalteam.dragonsurvival.common.capability;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber( modid = MODID, bus = EventBusSubscriber.Bus.MOD)
public class Capabilities{

	public static final EntityCapability<EntityStateHandler, Void> ENTITY_CAPABILITY = EntityCapability.createVoid(
			new ResourceLocation(MODID, "entity_capability"),
			EntityStateHandler.class);

	public static final EntityCapability<DragonStateHandler, Void> DRAGON_CAPABILITY = EntityCapability.createVoid(
			new ResourceLocation(MODID, "dragon_capability"),
			DragonStateHandler.class);

	@SubscribeEvent
	public static void register(RegisterCapabilitiesEvent event) {
		event.registerEntity(DRAGON_CAPABILITY, EntityType.PLAYER, new DragonStateProvider());
		event.registerEntity(ENTITY_CAPABILITY, EntityType.PLAYER, new EntityStateProvider());
	}
}