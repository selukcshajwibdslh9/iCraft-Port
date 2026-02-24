package iCraft.core;

import iCraft.core.item.ItemiCraft;
import iCraft.core.network.MessageConfigSync;
import iCraft.core.network.NetworkHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class CommonPlayerTracker {
    public CommonPlayerTracker() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        if (!event.player.world.isRemote) {
            NetworkHandler.sendTo(new MessageConfigSync(), (EntityPlayerMP) event.player);
            ICraft.logger.info("Sent config to '" + event.player.getDisplayName() + ".'");
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        NonNullList<ItemStack> inventory = event.player.inventory.mainInventory;

        for (ItemStack itemStack : inventory) {
            if (itemStack == null) continue;
            if (!(itemStack.getItem() instanceof ItemiCraft)) continue;
            if (itemStack.getTagCompound() == null) continue;
            if (!itemStack.getTagCompound().hasKey("called")) continue;
            if (itemStack.getTagCompound().getInteger("called") == 0) continue;
            if (!itemStack.getTagCompound().hasKey("isCalling")) continue;

            boolean isCalling = itemStack.getTagCompound().getBoolean("isCalling");
            String targetName = isCalling
                    ? itemStack.getTagCompound().getString("calledPlayer")
                    : itemStack.getTagCompound().getString("callingPlayer");
            int targetNumber = isCalling
                    ? itemStack.getTagCompound().getInteger("calledNumber")
                    : itemStack.getTagCompound().getInteger("callingNumber");

            for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
                if (!player.getName().equals(targetName)) continue;

                NonNullList<ItemStack> targetInventory = player.inventory.mainInventory;

                for (ItemStack stack : targetInventory) {
                    if (stack == null) continue;
                    if (!(stack.getItem() instanceof ItemiCraft)) continue;
                    if (stack.getTagCompound() == null) continue;

                    ItemiCraft iCraftItem = (ItemiCraft) stack.getItem();
                    if (iCraftItem.getNumber(stack) == targetNumber) {
                        stack.getTagCompound().setInteger("called", 0);
                        player.closeScreen();
                    }
                }
                break;
            }
        }
    }
}
