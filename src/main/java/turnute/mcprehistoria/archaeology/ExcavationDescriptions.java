package turnute.mcprehistoria.archaeology;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import turnute.mcprehistoria.registry.ModItems;
import turnute.mcprehistoria.registry.ModBlocks;

public class ExcavationDescriptions {
    private ExcavationDescriptions() {}

    private static final Map<Item, Component> DESCRIPTIONS = new HashMap<>();

    public static void register() {
        DESCRIPTIONS.put(ModBlocks.EXCAVATION_TABLE.asItem(), Component.literal("Excavation Table: Used for examining Minecraft Prehistorica mod items and blocks."));
    }

    public static Component getDescription(final Item item) {
        return DESCRIPTIONS.getOrDefault(item, Component.literal("There's nothing of note to this item..."));
    }
}