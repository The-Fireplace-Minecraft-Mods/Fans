package the_fireplace.fans.datagen;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.loot.*;
import net.minecraft.loot.condition.*;
import net.minecraft.loot.entry.DynamicEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.*;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.registry.Registry;
import the_fireplace.fans.Fans;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class BlockLootTableGenerator implements Consumer<BiConsumer<Identifier, LootTable.Builder>> {
    private static final LootCondition.Builder WITH_SILK_TOUCH;
    private static final LootCondition.Builder WITHOUT_SILK_TOUCH;
    private static final LootCondition.Builder WITH_SHEARS;
    private static final LootCondition.Builder WITH_SILK_TOUCH_OR_SHEARS;
    private static final LootCondition.Builder WITHOUT_SILK_TOUCH_NOR_SHEARS;
    private static final Set<Item> EXPLOSION_IMMUNE;
    private static final float[] SAPLING_DROP_CHANCE;
    private static final float[] JUNGLE_SAPLING_DROP_CHANCE;
    private final Map<Identifier, LootTable.Builder> lootTables = Maps.newHashMap();

    @Override
    public void accept(BiConsumer<Identifier, LootTable.Builder> identifierBuilderBiConsumer) {
        addDrop(Fans.FAN_BLOCK);


        Set<Identifier> set = Sets.newHashSet();

        for (Block block : Fans.BLOCKS) {
            Identifier identifier = block.getLootTableId();
            if (identifier != LootTables.EMPTY && set.add(identifier)) {
                LootTable.Builder builder5 = this.lootTables.remove(identifier);
                if (builder5 == null) {
                    System.err.println(String.format("Missing loottable '%s' for '%s'", identifier, Registry.BLOCK.getId(block)));
                    continue;
                }

                identifierBuilderBiConsumer.accept(identifier, builder5);
            }
        }

        if (!this.lootTables.isEmpty()) {
            throw new IllegalStateException("Created block loot tables for non-blocks: " + this.lootTables.keySet());
        }
    }

    private static <T> T applyExplosionDecay(ItemConvertible drop, LootFunctionConsumingBuilder<T> builder) {
        return !EXPLOSION_IMMUNE.contains(drop.asItem()) ? builder.apply(ExplosionDecayLootFunction.builder()) : builder.getThis();
    }

    private static <T> T addSurvivesExplosionCondition(ItemConvertible drop, LootConditionConsumingBuilder<T> builder) {
        return !EXPLOSION_IMMUNE.contains(drop.asItem()) ? builder.conditionally(SurvivesExplosionLootCondition.builder()) : builder.getThis();
    }

    private static LootTable.Builder drops(ItemConvertible drop) {
        return LootTable.builder().pool(addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootTableRange.create(1)).with(ItemEntry.builder(drop))));
    }

    private static LootTable.Builder drops(Block drop, LootCondition.Builder conditionBuilder, LootPoolEntry.Builder<?> child) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootTableRange.create(1)).with(ItemEntry.builder(drop).conditionally(conditionBuilder).alternatively(child)));
    }

    private static LootTable.Builder dropsWithSilkTouch(Block drop, LootPoolEntry.Builder<?> child) {
        return drops(drop, WITH_SILK_TOUCH, child);
    }

    private static LootTable.Builder dropsWithShears(Block drop, LootPoolEntry.Builder<?> child) {
        return drops(drop, WITH_SHEARS, child);
    }

    private static LootTable.Builder dropsWithSilkTouchOrShears(Block drop, LootPoolEntry.Builder<?> child) {
        return drops(drop, WITH_SILK_TOUCH_OR_SHEARS, child);
    }

    private static LootTable.Builder drops(Block dropWithSilkTouch, ItemConvertible drop) {
        return dropsWithSilkTouch(dropWithSilkTouch, addSurvivesExplosionCondition(dropWithSilkTouch, ItemEntry.builder(drop)));
    }

    private static LootTable.Builder drops(ItemConvertible drop, LootTableRange count) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootTableRange.create(1)).with(applyExplosionDecay(drop, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(count)))));
    }

    private static LootTable.Builder drops(Block dropWithSilkTouch, ItemConvertible drop, LootTableRange count) {
        return dropsWithSilkTouch(dropWithSilkTouch, applyExplosionDecay(dropWithSilkTouch, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(count))));
    }

    private static LootTable.Builder dropsWithSilkTouch(ItemConvertible drop) {
        return LootTable.builder().pool(LootPool.builder().conditionally(WITH_SILK_TOUCH).rolls(ConstantLootTableRange.create(1)).with(ItemEntry.builder(drop)));
    }

    private static LootTable.Builder pottedPlantDrops(ItemConvertible plant) {
        return LootTable.builder().pool(addSurvivesExplosionCondition(Blocks.FLOWER_POT, LootPool.builder().rolls(ConstantLootTableRange.create(1)).with(ItemEntry.builder(Blocks.FLOWER_POT)))).pool(addSurvivesExplosionCondition(plant, LootPool.builder().rolls(ConstantLootTableRange.create(1)).with(ItemEntry.builder(plant))));
    }

    private static LootTable.Builder slabDrops(Block drop) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootTableRange.create(1)).with(applyExplosionDecay(drop, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(ConstantLootTableRange.create(2)).conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(SlabBlock.TYPE, SlabType.DOUBLE)))))));
    }

    private static <T extends Comparable<T> & StringIdentifiable> LootTable.Builder dropsWithProperty(Block drop, Property<T> property, T comparable) {
        return LootTable.builder().pool(addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootTableRange.create(1)).with(ItemEntry.builder(drop).conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(property, comparable))))));
    }

    private static LootTable.Builder nameableContainerDrops(Block drop) {
        return LootTable.builder().pool(addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootTableRange.create(1)).with(ItemEntry.builder(drop).apply(CopyNameLootFunction.builder(CopyNameLootFunction.Source.BLOCK_ENTITY)))));
    }

    private static LootTable.Builder shulkerBoxDrops(Block drop) {
        return LootTable.builder().pool(addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootTableRange.create(1)).with(ItemEntry.builder(drop).apply(CopyNameLootFunction.builder(CopyNameLootFunction.Source.BLOCK_ENTITY)).apply(CopyNbtLootFunction.builder(CopyNbtLootFunction.Source.BLOCK_ENTITY).withOperation("Lock", "BlockEntityTag.Lock").withOperation("LootTable", "BlockEntityTag.LootTable").withOperation("LootTableSeed", "BlockEntityTag.LootTableSeed")).apply(SetContentsLootFunction.builder().withEntry(DynamicEntry.builder(ShulkerBoxBlock.CONTENTS))))));
    }

    private static LootTable.Builder bannerDrops(Block drop) {
        return LootTable.builder().pool(addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootTableRange.create(1)).with(ItemEntry.builder(drop).apply(CopyNameLootFunction.builder(CopyNameLootFunction.Source.BLOCK_ENTITY)).apply(CopyNbtLootFunction.builder(CopyNbtLootFunction.Source.BLOCK_ENTITY).withOperation("Patterns", "BlockEntityTag.Patterns")))));
    }

    private static LootTable.Builder beeNestDrops(Block drop) {
        return LootTable.builder().pool(LootPool.builder().conditionally(WITH_SILK_TOUCH).rolls(ConstantLootTableRange.create(1)).with(ItemEntry.builder(drop).apply(CopyNbtLootFunction.builder(CopyNbtLootFunction.Source.BLOCK_ENTITY).withOperation("Bees", "BlockEntityTag.Bees")).apply(CopyStateFunction.getBuilder(drop).method_21898(BeehiveBlock.HONEY_LEVEL))));
    }

    private static LootTable.Builder beehiveDrops(Block drop) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootTableRange.create(1)).with(ItemEntry.builder(drop).conditionally(WITH_SILK_TOUCH).apply(CopyNbtLootFunction.builder(CopyNbtLootFunction.Source.BLOCK_ENTITY).withOperation("Bees", "BlockEntityTag.Bees")).apply(CopyStateFunction.getBuilder(drop).method_21898(BeehiveBlock.HONEY_LEVEL)).alternatively(ItemEntry.builder(drop))));
    }

    private static LootTable.Builder oreDrops(Block dropWithSilkTouch, Item drop) {
        return dropsWithSilkTouch(dropWithSilkTouch, applyExplosionDecay(dropWithSilkTouch, ItemEntry.builder(drop).apply(ApplyBonusLootFunction.oreDrops(Enchantments.FORTUNE))));
    }

    private static LootTable.Builder mushroomBlockDrops(Block dropWithSilkTouch, ItemConvertible drop) {
        return dropsWithSilkTouch(dropWithSilkTouch, applyExplosionDecay(dropWithSilkTouch, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(UniformLootTableRange.between(-6.0F, 2.0F))).apply(LimitCountLootFunction.builder(BoundedIntUnaryOperator.createMin(0)))));
    }

    private static LootTable.Builder grassDrops(Block dropWithShears) {
        return dropsWithShears(dropWithShears, applyExplosionDecay(dropWithShears, ItemEntry.builder(Items.WHEAT_SEEDS).conditionally(RandomChanceLootCondition.builder(0.125F)).apply(ApplyBonusLootFunction.uniformBonusCount(Enchantments.FORTUNE, 2))));
    }

    private static LootTable.Builder cropStemDrops(Block stem, Item drop) {
        return LootTable.builder().pool(applyExplosionDecay(stem, LootPool.builder().rolls(ConstantLootTableRange.create(1)).with(ItemEntry.builder(drop).apply(SetCountLootFunction.builder(BinomialLootTableRange.create(3, 0.06666667F)).conditionally(BlockStatePropertyLootCondition.builder(stem).properties(StatePredicate.Builder.create().exactMatch(StemBlock.AGE, 0)))).apply(SetCountLootFunction.builder(BinomialLootTableRange.create(3, 0.13333334F)).conditionally(BlockStatePropertyLootCondition.builder(stem).properties(StatePredicate.Builder.create().exactMatch(StemBlock.AGE, 1)))).apply(SetCountLootFunction.builder(BinomialLootTableRange.create(3, 0.2F)).conditionally(BlockStatePropertyLootCondition.builder(stem).properties(StatePredicate.Builder.create().exactMatch(StemBlock.AGE, 2)))).apply(SetCountLootFunction.builder(BinomialLootTableRange.create(3, 0.26666668F)).conditionally(BlockStatePropertyLootCondition.builder(stem).properties(StatePredicate.Builder.create().exactMatch(StemBlock.AGE, 3)))).apply(SetCountLootFunction.builder(BinomialLootTableRange.create(3, 0.33333334F)).conditionally(BlockStatePropertyLootCondition.builder(stem).properties(StatePredicate.Builder.create().exactMatch(StemBlock.AGE, 4)))).apply(SetCountLootFunction.builder(BinomialLootTableRange.create(3, 0.4F)).conditionally(BlockStatePropertyLootCondition.builder(stem).properties(StatePredicate.Builder.create().exactMatch(StemBlock.AGE, 5)))).apply(SetCountLootFunction.builder(BinomialLootTableRange.create(3, 0.46666667F)).conditionally(BlockStatePropertyLootCondition.builder(stem).properties(StatePredicate.Builder.create().exactMatch(StemBlock.AGE, 6)))).apply(SetCountLootFunction.builder(BinomialLootTableRange.create(3, 0.53333336F)).conditionally(BlockStatePropertyLootCondition.builder(stem).properties(StatePredicate.Builder.create().exactMatch(StemBlock.AGE, 7)))))));
    }

    private static LootTable.Builder attachedCropStemDrops(Block stem, Item drop) {
        return LootTable.builder().pool(applyExplosionDecay(stem, LootPool.builder().rolls(ConstantLootTableRange.create(1)).with(ItemEntry.builder(drop).apply(SetCountLootFunction.builder(BinomialLootTableRange.create(3, 0.53333336F))))));
    }

    private static LootTable.Builder dropsWithShears(ItemConvertible drop) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootTableRange.create(1)).conditionally(WITH_SHEARS).with(ItemEntry.builder(drop)));
    }

    private static LootTable.Builder leavesDrop(Block leaves, Block drop, float... chance) {
        return dropsWithSilkTouchOrShears(leaves, addSurvivesExplosionCondition(leaves, ItemEntry.builder(drop)).conditionally(TableBonusLootCondition.builder(Enchantments.FORTUNE, chance))).pool(LootPool.builder().rolls(ConstantLootTableRange.create(1)).conditionally(WITHOUT_SILK_TOUCH_NOR_SHEARS).with(applyExplosionDecay(leaves, ItemEntry.builder(Items.STICK).apply(SetCountLootFunction.builder(UniformLootTableRange.between(1.0F, 2.0F)))).conditionally(TableBonusLootCondition.builder(Enchantments.FORTUNE, 0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F))));
    }

    private static LootTable.Builder oakLeavesDrop(Block leaves, Block drop, float... chance) {
        return leavesDrop(leaves, drop, chance).pool(LootPool.builder().rolls(ConstantLootTableRange.create(1)).conditionally(WITHOUT_SILK_TOUCH_NOR_SHEARS).with(addSurvivesExplosionCondition(leaves, ItemEntry.builder(Items.APPLE)).conditionally(TableBonusLootCondition.builder(Enchantments.FORTUNE, 0.005F, 0.0055555557F, 0.00625F, 0.008333334F, 0.025F))));
    }

    private static LootTable.Builder cropDrops(Block crop, Item product, Item seeds, LootCondition.Builder condition) {
        return applyExplosionDecay(crop, LootTable.builder().pool(LootPool.builder().with(ItemEntry.builder(product).conditionally(condition).alternatively(ItemEntry.builder(seeds)))).pool(LootPool.builder().conditionally(condition).with(ItemEntry.builder(seeds).apply(ApplyBonusLootFunction.binomialWithBonusCount(Enchantments.FORTUNE, 0.5714286F, 3)))));
    }

    public static LootTable.Builder dropsNothing() {
        return LootTable.builder();
    }

    public void addPottedPlantDrop(Block block) {
        this.addDrop(block, (blockx) -> pottedPlantDrops(((FlowerPotBlock)blockx).getContent()));
    }

    public void addDropWithSilkTouch(Block block, Block droppedBlock) {
        this.addDrop(block, dropsWithSilkTouch(droppedBlock));
    }

    public void addDrop(Block block, ItemConvertible loot) {
        this.addDrop(block, drops(loot));
    }

    public void addDropWithSilkTouch(Block block) {
        this.addDropWithSilkTouch(block, block);
    }

    public void addDrop(Block block) {
        this.addDrop(block, block);
    }

    private void addDrop(Block block, Function<Block, LootTable.Builder> function) {
        this.addDrop(block, function.apply(block));
    }

    private void addDrop(Block block, LootTable.Builder builder) {
        this.lootTables.put(block.getLootTableId(), builder);
    }

    static {
        WITH_SILK_TOUCH = MatchToolLootCondition.builder(ItemPredicate.Builder.create().enchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, NumberRange.IntRange.atLeast(1))));
        WITHOUT_SILK_TOUCH = WITH_SILK_TOUCH.invert();
        WITH_SHEARS = MatchToolLootCondition.builder(ItemPredicate.Builder.create().item(Items.SHEARS));
        WITH_SILK_TOUCH_OR_SHEARS = WITH_SHEARS.or(WITH_SILK_TOUCH);
        WITHOUT_SILK_TOUCH_NOR_SHEARS = WITH_SILK_TOUCH_OR_SHEARS.invert();
        EXPLOSION_IMMUNE = Stream.of(Blocks.DRAGON_EGG, Blocks.BEACON, Blocks.CONDUIT, Blocks.SKELETON_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.PLAYER_HEAD, Blocks.ZOMBIE_HEAD, Blocks.CREEPER_HEAD, Blocks.DRAGON_HEAD, Blocks.SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX).map(ItemConvertible::asItem).collect(ImmutableSet.toImmutableSet());
        SAPLING_DROP_CHANCE = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};
        JUNGLE_SAPLING_DROP_CHANCE = new float[]{0.025F, 0.027777778F, 0.03125F, 0.041666668F, 0.1F};
    }
}

