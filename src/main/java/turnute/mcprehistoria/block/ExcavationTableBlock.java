package turnute.mcprehistoria.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import turnute.mcprehistoria.archaeology.ExcavationDescriptions;

public class ExcavationTableBlock extends Block {

    public ExcavationTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if(level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if(stack.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.minecraft-prehistoria.excavation_table.empty"));
            return InteractionResult.SUCCESS;
        }

        player.sendSystemMessage(ExcavationDescriptions.getDescription(stack.getItem()));

        return InteractionResult.SUCCESS;

    }
}
