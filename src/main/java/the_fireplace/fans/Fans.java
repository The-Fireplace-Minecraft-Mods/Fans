package the_fireplace.fans;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import the_fireplace.fans.block.FanBlock;
import the_fireplace.fans.block.entity.FanBlockEntity;
import the_fireplace.fans.datagen.AdditiveDataGenerator;
import the_fireplace.fans.datagen.LootTablesProvider;
import the_fireplace.fans.datagen.RecipesProvider;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;

public class Fans implements ModInitializer {
    public static final String MODID = "fans";
    @Override
    public void onInitialize() {
        registerBlocks();
        registerBlockEntities();
        registerItems();

        //noinspection ConstantConditions//TODO Use environment variables for this
        if(true) {
            DataGenerator gen = new AdditiveDataGenerator(Paths.get("..", "src", "main", "resources"), Collections.emptySet());
            gen.install(new RecipesProvider(gen));
            gen.install(new LootTablesProvider(gen));
            try {
                gen.run();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    //Blocks
    public static final Block FAN_BLOCK = new FanBlock(FabricBlockSettings.copy(Blocks.IRON_BLOCK));
    public static final ImmutableList<Block> BLOCKS = ImmutableList.of(FAN_BLOCK);
    //Block Entities
    public static BlockEntityType<FanBlockEntity> FAN_BLOCK_ENTITY;
    public static final Identifier FAN_BLOCK_ENTITY_ID = new Identifier(MODID, "fan_block");
    //Items
    public static final Item DIAMOND_FAN = new Item(new Item.Settings().group(ItemGroup.MISC));

    public static void registerBlocks() {
        registerBlockWithItem("fan_block", FAN_BLOCK, ItemGroup.REDSTONE);
    }

    public static void registerBlockEntities() {
        FAN_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, FAN_BLOCK_ENTITY_ID,
            BlockEntityType.Builder.create(FanBlockEntity::new, FAN_BLOCK).build(null));
        ContainerProviderRegistry.INSTANCE.registerFactory(FAN_BLOCK_ENTITY_ID, (syncId, identifier, player, buf) -> {
            final World world = player.world;
            final BlockPos pos = buf.readBlockPos();
            return Objects.requireNonNull(world.getBlockState(pos).createScreenHandlerFactory(player.world, pos))
                .createMenu(syncId, player.inventory, player);
        });
    }

    public static void registerItems() {
        registerItem("diamond_fan", DIAMOND_FAN);
    }

    private static void registerItem(String path, Item item) {
        Registry.register(Registry.ITEM, new Identifier(MODID, path), item);
    }

    private static void registerBlock(String path, Block block) {
        Registry.register(Registry.BLOCK, new Identifier(MODID, path), block);
    }

    private static void registerBlockWithItem(String path, Block block, ItemGroup group) {
        registerBlock(path, block);
        registerItem(path, new BlockItem(block, new Item.Settings().group(group)));
    }
}
