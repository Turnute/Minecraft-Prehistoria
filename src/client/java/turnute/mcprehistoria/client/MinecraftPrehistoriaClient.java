package turnute.mcprehistoria.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import turnute.mcprehistoria.client.render.GiantSquidRenderer;
import turnute.mcprehistoria.registry.ModEntities;

public class MinecraftPrehistoriaClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntities.GIANT_SQUID, GiantSquidRenderer::new);
	}
}