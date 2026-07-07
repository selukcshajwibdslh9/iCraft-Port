package iCraft.core.utils;

import iCraft.core.ICraft;
import iCraft.core.item.ItemiCraft;
import java.util.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ICraftUtils {
    
    public static final List<TradeOffer> tradeOffers = new ArrayList<>();

    public static String localize(String s) {
        return I18n.translateToLocal(s);
    }

    public static void addBuyableItems(int index, ItemStack buyStack, ItemStack sellStack) {
        if (buyStack != null) {
            tradeOffers.add(new TradeOffer(sellStack, buyStack));
        }
    }

public static void loadTradeOffers() {
    tradeOffers.clear(); 
    // Вместо чтения из файла, берем массив, который пришел в пакете
    String[] items = ICraft.buyableItems; 
    
    for (String entry : items) {
        try {
                String[] parts = entry.split("-");
                if (parts.length == 2) {
                    String[] buy = parts[0].split("\\.");
                    ItemStack buyStack = new ItemStack(net.minecraft.item.Item.getItemById(Integer.parseInt(buy[0])), Integer.parseInt(buy[1]), Integer.parseInt(buy[2]));
                    
                    String[] sell = parts[1].split("\\.");
                    ItemStack sellStack = new ItemStack(net.minecraft.item.Item.getItemById(Integer.parseInt(sell[0])), Integer.parseInt(sell[1]), Integer.parseInt(sell[2]));
                    
                    tradeOffers.add(new TradeOffer(sellStack, buyStack));
                }
            } catch (Exception e) {
                System.err.println("Ошибка в строке конфига: " + entry);
            }
        }
    }

    public static void changeCalledStatus(ItemStack itemStack, int status, int status2, boolean isCalling) {
        if (itemStack != null && itemStack.getTagCompound() != null) {
            itemStack.getTagCompound().setInteger("called", status);
            if (status == 2) {
                itemStack.getTagCompound().setInteger("callCode", itemStack.getTagCompound().getInteger("number"));
            } else if (status == 0) {
                itemStack.getTagCompound().setInteger("callCode", 0);
                if (itemStack.getTagCompound().hasKey("isCalling")) {
                    itemStack.getTagCompound().setBoolean("isCalling", false);
                }
            }

            Iterator i = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers().iterator();
            while (i.hasNext()) {
                EntityPlayerMP players = (EntityPlayerMP) i.next();
                if (players.getName().equals(isCalling ? itemStack.getTagCompound().getString("calledPlayer") : itemStack.getTagCompound().getString("callingPlayer"))) {
                    
                    List<NonNullList<ItemStack>> stacks = Arrays.asList(players.inventory.mainInventory);
                    Iterator it = stacks.iterator();
                    while (it.hasNext()) {
                        ItemStack stack = (ItemStack) it.next();
                        if (stack != null && stack.getItem() instanceof ItemiCraft) {
                            ItemiCraft iCraft = (ItemiCraft) stack.getItem();
                            if (stack.getTagCompound() != null && iCraft.getNumber(stack) == (isCalling ? itemStack.getTagCompound().getInteger("calledNumber") : itemStack.getTagCompound().getInteger("callingNumber"))) {
                                stack.getTagCompound().setInteger("called", status2);
                                if (status2 == 2) {
                                    stack.getTagCompound().setInteger("callCode", itemStack.getTagCompound().getInteger("number"));
                                } else if (status2 == 0) {
                                    stack.getTagCompound().setInteger("callCode", 0);
                                    if (stack.getTagCompound().hasKey("isCalling")) {
                                        stack.getTagCompound().setBoolean("isCalling", false);
                                    }
                                    return;
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList();
        list.addAll((new ConfigElement(ICraft.configuration.getCategory("general"))).getChildElements());
        list.addAll((new ConfigElement(ICraft.configuration.getCategory("voice settings"))).getChildElements());
        return list;
    }
}