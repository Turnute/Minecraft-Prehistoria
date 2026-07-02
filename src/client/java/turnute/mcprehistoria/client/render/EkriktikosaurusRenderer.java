package turnute.mcprehistoria.client.render;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import turnute.mcprehistoria.MinecraftPrehistoria;
import turnute.mcprehistoria.entity.dino.EkriktikosaurusEntity;
import turnute.mcprehistoria.registry.ModEntities;

public class EkriktikosaurusRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<EkriktikosaurusEntity, R> {

    private static final DataTicket<Boolean> FLASH_WHITE =
            DataTicket.create("mcprehistoria_ekriktikosaurus_flash_white", Boolean.class);

    private static final Identifier NORMAL_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    MinecraftPrehistoria.MOD_ID,
                    "textures/entity/ekriktikosaurus.png"
            );

    private static final Identifier FLASH_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    MinecraftPrehistoria.MOD_ID,
                    "textures/entity/ekriktikosaurus_flash.png"
            );

    public EkriktikosaurusRenderer(EntityRendererProvider.Context context) {
        super(context, ModEntities.EKRIKTIKOSAURUS);
    }

    @Override
    public void addRenderData(
            EkriktikosaurusEntity animatable,
            @Nullable Void relatedObject,
            R renderState,
            float partialTick
    ) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);

        renderState.addGeckolibData(FLASH_WHITE, animatable.shouldFlashWhite());
    }

    @Override
    public Identifier getTextureLocation(R renderState) {
        return renderState.getOrDefaultGeckolibData(FLASH_WHITE, false)
                ? FLASH_TEXTURE
                : NORMAL_TEXTURE;
    }
}