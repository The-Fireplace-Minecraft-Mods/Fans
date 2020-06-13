package the_fireplace.fans.block.entity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import the_fireplace.fans.Fans;
import the_fireplace.fans.block.FanBlock;
import the_fireplace.fans.screen.Generic1x1ContainerScreenHandler;

public class FanBlockEntity extends LootableContainerBlockEntity implements Clearable, Tickable, BlockEntityClientSerializable {
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    public FanBlockEntity() {
        super(Fans.FAN_BLOCK_ENTITY);
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        return inventory;
    }

    @Override
    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }

    @Override
    protected Text getContainerName() {
        return new LiteralText("Fan Case");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new Generic1x1ContainerScreenHandler(syncId, playerInventory);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack stack = super.removeStack(slot, amount);
        updateListeners();
        return stack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = super.removeStack(slot);
        updateListeners();
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        super.setStack(slot, stack);
        updateListeners();
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(tag))
            Inventories.fromTag(tag, this.inventory);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        if (!this.serializeLootTable(tag))
            Inventories.toTag(tag, this.inventory);

        return tag;
    }

    /**
     * Make sure the client knows what stack is there, if any
     */
    private void updateListeners() {
        //this.markDirty();
        this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), 3);
    }

    @Override
    public void fromClientTag(CompoundTag compoundTag) {
        Inventories.fromTag(compoundTag, this.inventory);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag compoundTag) {
        return Inventories.toTag(compoundTag, this.inventory);
    }

    @Override
    public void tick() {
        //TODO power levels?
        if(getCachedState().get(FanBlock.POWERED)) {
            //for()
        }
    }
}
