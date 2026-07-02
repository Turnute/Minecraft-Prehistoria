package turnute.mcprehistoria.archaeology;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import turnute.mcprehistoria.registry.ModBlocks;
import turnute.mcprehistoria.registry.ModItems;

/**
 * Small, hardcoded archaeology codex used by the excavation table.
 *
 * The table stays deliberately simple for the jam: it reads the item held by the
 * player and returns a short translated description when the object belongs to
 * the mod.
 */
public final class ExcavationDescriptions {
    private ExcavationDescriptions() {}

    private static final Map<Item, Component> DESCRIPTIONS = new HashMap<>();

    public static void register() {
        // Blocks
        add(ModBlocks.EXCAVATION_TABLE.asItem(), "excavation_table");
        add(ModBlocks.BLUE_MUSHROOM.asItem(), "blue_mushroom");

        // Fossils and finds
        add(ModItems.ANCIENT_BONE, "ancient_bone");

        // Hatchable eggs
        add(ModBlocks.MEGAMELI_EGG.asItem(), "megameli_egg");
        add(ModBlocks.ODONTHERIUM_EGG.asItem(), "odontherium_egg");

        // Spawn eggs
        add(ModItems.GIANT_SQUID_SPAWN_EGG, "giant_squid_spawn_egg");
        add(ModItems.MEGAMELI_SPAWN_EGG, "megameli_spawn_egg");
        add(ModItems.ODONTHERIUM_SPAWN_EGG, "odontherium_spawn_egg");
        add(ModItems.EKRIKTIKOSAURUS_SPAWN_EGG, "ekriktikosaurus_spawn_egg");
    }

    private static void add(Item item, String descriptionKey) {
        DESCRIPTIONS.put(
                item,
                Component.translatable("excavation.minecraft-prehistoria." + descriptionKey)
        );
    }

    public static Component getDescription(final Item item) {
        return DESCRIPTIONS.getOrDefault(
                item,
                Component.translatable("message.minecraft-prehistoria.excavation_table.default")
        );
    }
}
