package iCraft.core.utils;

import net.minecraft.item.ItemStack;

public class TradeOffer {
    public ItemStack sellStack;
    public ItemStack buyStack;
    
    public TradeOffer(ItemStack sell, ItemStack buy) {
        this.sellStack = sell;
        this.buyStack = buy;
    }
}