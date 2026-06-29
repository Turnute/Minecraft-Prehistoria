package turnute.mcprehistoria.client.render;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import turnute.mcprehistoria.MinecraftPrehistoria;
import turnute.mcprehistoria.entity.dino.OdontheriumEntity;
import turnute.mcprehistoria.registry.ModEntities;

public class OdontheriumRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<OdontheriumEntity, R> {

    private static final DataTicket<Boolean> IS_AGGRO =
            DataTicket.create("mcprehistoria_odontherium_aggro", Boolean.class);

    private static final Identifier NORMAL_TEXTURE =
            Identifier.fromNamespaceAndPath(MinecraftPrehistoria.MOD_ID, "textures/entity/odontherium.png");

    private static final Identifier AGGRO_TEXTURE =
            Identifier.fromNamespaceAndPath(MinecraftPrehistoria.MOD_ID, "textures/entity/odontherium_aggro.png");

    public OdontheriumRenderer(EntityRendererProvider.Context context) {
        super(context, ModEntities.ODONTHERIUM);
    }

    @Override
    public void addRenderData(
            OdontheriumEntity animatable,
            @Nullable Void relatedObject,
            R renderState,
            float partialTick
    ) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);

        renderState.addGeckolibData(IS_AGGRO, animatable.isAggressive());
    }

    @Override
    public Identifier getTextureLocation(R renderState) {
        return renderState.getOrDefaultGeckolibData(IS_AGGRO, false)
                ? AGGRO_TEXTURE
                : NORMAL_TEXTURE;
    }
}