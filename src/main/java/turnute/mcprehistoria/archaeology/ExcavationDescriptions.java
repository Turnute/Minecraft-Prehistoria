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
        // Blocks
        DESCRIPTIONS.put(ModBlocks.EXCAVATION_TABLE.asItem(), Component.literal("Excavation Table: Used for examining Minecraft Prehistorica mod items and blocks."));

        // Items & Plants
        DESCRIPTIONS.put(ModItems.ANCIENT_BONE, Component.literal("Ancient Bone: TODO"));
        DESCRIPTIONS.put(ModBlocks.BLUE_MUSHROOM.asItem(), Component.literal("Blue Mushroom: An ancient species of mushrooms that survived in a remote place. Emits a bright natural light."));

        // Hatchable Eggs
        DESCRIPTIONS.put(ModBlocks.MEGAMELI_EGG.asItem(), Component.literal("Megameli Egg: TODO"));

        // Spawn Eggs
        DESCRIPTIONS.put(ModItems.MEGAMELI_SPAWN_EGG, Component.literal("Megameli Spawn Egg: Spawns a Megameli Entity."));
        DESCRIPTIONS.put(ModItems.GIANT_SQUID_SPAWN_EGG, Component.literal("Giant Squid Spawn Egg: Spawns a Giant Squid Entity."));

    }

    public static Component getDescription(final Item item) {
        return DESCRIPTIONS.getOrDefault(item, Component.literal("There's nothing of note to this item..."));
    }
}