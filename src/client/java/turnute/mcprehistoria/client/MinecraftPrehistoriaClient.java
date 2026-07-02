package turnute.mcprehistoria.client;

import com.geckolib.renderer.GeoEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import turnute.mcprehistoria.client.render.EkriktikosaurusRenderer;
import turnute.mcprehistoria.client.render.GiantSquidRenderer;
import turnute.mcprehistoria.client.render.OdontheriumRenderer;
import turnute.mcprehistoria.registry.ModEntities;

public class MinecraftPrehistoriaClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntities.GIANT_SQUID, GiantSquidRenderer::new);
		EntityRenderers.register(ModEntities.MEGAMELI, context -> new GeoEntityRenderer<>(context, ModEntities.MEGAMELI));
		EntityRenderers.register(ModEntities.ODONTHERIUM, OdontheriumRenderer::new);
		EntityRenderers.register(ModEntities.EKRIKTIKOSAURUS, EkriktikosaurusRenderer::new);
	}
}