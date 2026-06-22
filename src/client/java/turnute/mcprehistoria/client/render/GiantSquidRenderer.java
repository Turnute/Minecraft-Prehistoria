package turnute.mcprehistoria.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.animal.squid.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.client.renderer.entity.state.SquidRenderState;
import turnute.mcprehistoria.entity.aquatic.GiantSquidEntity;

public class GiantSquidRenderer extends SquidRenderer<GiantSquidEntity> {

    public GiantSquidRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new SquidModel(context.bakeLayer(ModelLayers.SQUID)),
                new SquidModel(context.bakeLayer(ModelLayers.SQUID_BABY))
        );
    }

    @Override
    public SquidRenderState createRenderState() {
        return new GiantSquidRenderState();
    }

    @Override
    public void extractRenderState(GiantSquidEntity entity, SquidRenderState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);

        if (state instanceof GiantSquidRenderState giantState) {
            giantState.sizeMultiplier = entity.getSizeMultiplier();
        }
    }

    @Override
    protected void scale(SquidRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);

        if (state instanceof GiantSquidRenderState giantState) {
            float size = giantState.sizeMultiplier;
            poseStack.scale(size, size, size);
        }
    }

    @Override
    protected float getShadowRadius(SquidRenderState state) {
        float baseShadow = super.getShadowRadius(state);

        if (state instanceof GiantSquidRenderState giantState) {
            return baseShadow * giantState.sizeMultiplier;
        }

        return baseShadow;
    }
}