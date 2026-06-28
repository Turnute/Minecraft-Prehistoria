package turnute.mcprehistoria.registry;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import turnute.mcprehistoria.MinecraftPrehistoria;

public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final ResourceKey<CreativeModeTab> PREHISTORIA_TAB_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            MinecraftPrehistoria.id("prehistoria")
    );

    public static final CreativeModeTab PREHISTORIA_TAB = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(Items.BONE))
            .title(Component.translatable("itemGroup.minecraft-prehistoria.prehistoria"))
            .displayItems((parameters, output) -> {

                // ========================
                // !!! ADD BLOCK & ITEMS HERE !!!

                // Blocks
                output.accept(ModBlocks.EXCAVATION_TABLE);

                // Plants & Items
                output.accept(ModBlocks.BLUE_MUSHROOM);
                output.accept(ModItems.ANCIENT_BONE);

                // Hatchable Eggs
                output.accept(ModBlocks.MEGAMELI_EGG);

                // Spawn Eggs
                output.accept(ModItems.GIANT_SQUID_SPAWN_EGG);
                output.accept(ModItems.MEGAMELI_SPAWN_EGG);
                output.accept(ModItems.ODONTHERIUM_SPAWN_EGG);

                // !!! ADD BLOCK & ITEMS HERE !!!
                // ========================
            })
            .build();

    public static void register() {
        MinecraftPrehistoria.LOGGER.info("Registering creative tabs for {}", MinecraftPrehistoria.MOD_NAME);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, PREHISTORIA_TAB_KEY, PREHISTORIA_TAB);
    }
}