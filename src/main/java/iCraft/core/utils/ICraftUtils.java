package iCraft.core.utils;

import iCraft.core.ICraft;
import iCraft.core.item.ItemiCraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ICraftUtils {
    public static final HashMap<Integer, HashMap<ItemStack, ItemStack>> items = new HashMap<>();

    public static String localize(String s) {
        return I18n.translateToLocal(s);
    }

    public static void changeCalledStatus(ItemStack itemStack, int status, int status2, boolean isCalling) {
        if (itemStack == null || !itemStack.hasTagCompound()) {
            return;
        }

        itemStack.getTagCompound().setInteger("called", status);

        if (status == 2) {

            itemStack.getTagCompound().setInteger(
                    "callCode",
                    itemStack.getTagCompound().getInteger("number")
            );

        } else if (status == 0) {

            itemStack.getTagCompound().setInteger("callCode", 0);

            if (itemStack.getTagCompound().hasKey("isCalling")) {
                itemStack.getTagCompound().setBoolean("isCalling", false);
            }
        }

        if (FMLCommonHandler.instance().getMinecraftServerInstance() == null) {
            return;
        }

        String targetPlayerName = isCalling
                ? itemStack.getTagCompound().getString("calledPlayer")
                : itemStack.getTagCompound().getString("callingPlayer");

        int targetNumber = isCalling
                ? itemStack.getTagCompound().getInteger("calledNumber")
                : itemStack.getTagCompound().getInteger("callingNumber");

        for (EntityPlayerMP player :
                FMLCommonHandler.instance()
                        .getMinecraftServerInstance()
                        .getPlayerList()
                        .getPlayers()) {

            if (!player.getName().equals(targetPlayerName)) {
                continue;
            }

            for (ItemStack stack : player.inventory.mainInventory) {

                if (stack.isEmpty()) {
                    continue;
                }

                if (!(stack.getItem() instanceof ItemiCraft)) {
                    continue;
                }

                if (!stack.hasTagCompound()) {
                    continue;
                }

                ItemiCraft iCraft = (ItemiCraft) stack.getItem();

                if (iCraft.getNumber(stack) != targetNumber) {
                    continue;
                }

                stack.getTagCompound().setInteger("called", status2);

                if (status2 == 2) {

                    stack.getTagCompound().setInteger(
                            "callCode",
                            itemStack.getTagCompound().getInteger("number")
                    );

                } else if (status2 == 0) {

                    stack.getTagCompound().setInteger("callCode", 0);

                    if (stack.getTagCompound().hasKey("isCalling")) {
                        stack.getTagCompound().setBoolean("isCalling", false);
                    }
                }

                return;
            }

            return;
        }
    }

    public static void addBuyableItems(int index, ItemStack buyStack, ItemStack sellStack) {
        if (buyStack == null || buyStack.isEmpty()) {
            return;
        }

        for (Map.Entry<Integer, HashMap<ItemStack, ItemStack>> entry :
                items.entrySet()) {

            if (entry.getValue().containsKey(buyStack)) {
                return;
            }
        }

        HashMap<ItemStack, ItemStack> map = new HashMap<>();
        map.put(buyStack, sellStack);

        items.put(index, map);
    }

    public static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<>();
        list.addAll(new ConfigElement(ICraft.configuration.getCategory("general")).getChildElements());
        list.addAll(new ConfigElement(ICraft.configuration.getCategory("voice settings")).getChildElements());

        return list;
    }
}
