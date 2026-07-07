package iCraft.core.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class TileEntityPackingCase extends TileEntity {
    public int itemIndex = -1;
    public int amountToBuy = 1; // По умолчанию 1
    public ItemStack tradeStack = ItemStack.EMPTY; // То, что выдаем
    public ItemStack buyStack = ItemStack.EMPTY;   // То, за что покупаем

@Override
   public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
    this.itemIndex = compound.getInteger("itemIndex");
    this.amountToBuy = compound.getInteger("amountToBuy");
        // Читаем стеки из NBT
        if (compound.hasKey("tradeStack")) {
            this.tradeStack = new ItemStack(compound.getCompoundTag("tradeStack"));
        }
        if (compound.hasKey("buyStack")) {
            this.buyStack = new ItemStack(compound.getCompoundTag("buyStack"));
        }
    }

@Override
   public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    super.writeToNBT(compound);
    compound.setInteger("itemIndex", this.itemIndex);
    compound.setInteger("amountToBuy", this.amountToBuy);
        
        // Пишем стеки в NBT
        if (!this.tradeStack.isEmpty()) {
            compound.setTag("tradeStack", this.tradeStack.writeToNBT(new NBTTagCompound()));
        }
        if (!this.buyStack.isEmpty()) {
            compound.setTag("buyStack", this.buyStack.writeToNBT(new NBTTagCompound()));
        }
        return compound;
    }
}