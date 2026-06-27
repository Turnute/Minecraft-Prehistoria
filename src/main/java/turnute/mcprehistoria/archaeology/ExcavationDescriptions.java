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
        DESCRIPTIONS.put(ModBlocks.BLUE_MUSHROOM.asItem(), Component.literal("Blue Mushroom: An ancient species of mushrooms that survived in a remote place. Emits a bright natural light."));
        DESCRIPTIONS.put(ModItems.GIANT_SQUID_SPAWN_EGG, Component.literal("Giant Squid Spawn Egg: Spawns a Giant Squid Entity."));
        DESCRIPTIONS.put(ModItems.MEGAMELI_SPAWN_EGG, Component.literal("Megameli Spawn Egg: Spawns a Megameli Entity."));
    }

    public static Component getDescription(final Item item) {
        return DESCRIPTIONS.getOrDefault(item, Component.literal("There's nothing of note to this item..."));
    }
}