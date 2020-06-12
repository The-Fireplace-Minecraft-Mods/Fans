package the_fireplace.fans.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class Generic1x1ContainerScreenHandler extends ScreenHandler {
   //public static final ScreenHandlerType<Generic1x1ContainerScreenHandler> GENERIC_1X1 = ScreenHandlerType.register("generic_1x1", Generic1x1ContainerScreenHandler::new);
   private final Inventory inventory;
   private static final int INVENTORY_SIZE = 1;

   public Generic1x1ContainerScreenHandler(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, new SimpleInventory(1));
   }

   public Generic1x1ContainerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
      super(null, syncId);
      checkSize(inventory, INVENTORY_SIZE);
      this.inventory = inventory;
      inventory.onOpen(playerInventory.player);

      this.addSlot(new Slot(inventory, 0, 62 + 18, 17 + 18));

      int m;
      int l;
      for(m = 0; m < 3; ++m)
         for(l = 0; l < 9; ++l)
            this.addSlot(new Slot(playerInventory, l + m * 9 + 1, 8 + l * 18, 84 + m * 18));

      for(m = 0; m < 9; ++m)
         this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
   }

   @Override
   public boolean canUse(PlayerEntity player) {
      return this.inventory.canPlayerUse(player);
   }

   @Override
   public ItemStack transferSlot(PlayerEntity player, int index) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = this.slots.get(index);
      if (slot != null && slot.hasStack()) {
         ItemStack itemStack2 = slot.getStack();
         itemStack = itemStack2.copy();
         if (index < 9) {
            if (!this.insertItem(itemStack2, 9, 45, true))
               return ItemStack.EMPTY;
         } else if (!this.insertItem(itemStack2, 0, 9, false))
            return ItemStack.EMPTY;

         if (itemStack2.isEmpty())
            slot.setStack(ItemStack.EMPTY);
         else
            slot.markDirty();

         if (itemStack2.getCount() == itemStack.getCount())
            return ItemStack.EMPTY;

         slot.onTakeItem(player, itemStack2);
      }

      return itemStack;
   }

   @Override
   public void close(PlayerEntity player) {
      super.close(player);
      this.inventory.onClose(player);
   }
}
