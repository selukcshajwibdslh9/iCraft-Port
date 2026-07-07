package iCraft.core;

import iCraft.core.item.ItemiCraft;
import iCraft.core.network.MessageClosePlayer;
import iCraft.core.network.NetworkHandler;
import iCraft.core.utils.ICraftUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ICraftEventHandler {
    public ICraftEventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onItemExpire(ItemExpireEvent event) {
        if (event.getEntityItem() != null && event.getEntityItem().getItem() != null) {
            ItemStack iCraft = event.getEntityItem().getItem();
            if (iCraft.getItem() instanceof ItemiCraft) {
                if (iCraft.getTagCompound() != null && iCraft.getTagCompound().hasKey("called") && iCraft.getTagCompound().getInteger("called") != 0) {
                    ICraftUtils.changeCalledStatus(iCraft, 0, 0, iCraft.getTagCompound().getBoolean("isCalling"));
                }
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals("iCraft")) {
            ICraft.proxy.loadConfiguration();
        }
    }
}