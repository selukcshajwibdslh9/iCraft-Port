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
        // МЫ ВСЁ ТУТ ВЫРЕЗАЛИ К ЧЕРТЯМ
        return; 
    }
}
